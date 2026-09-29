# Reproduces the official 40r12 installer byte for byte, from the data in
# installer/legacy-40r12.txt. Only a build with that installer id is affected.
# The next version drops this module, installer/legacy-40r12.txt, installer/RawDeflate.java
# and the legacy40r12 calls in build.py.
import base64, hashlib, os, shutil, struct, subprocess, tempfile, zipfile

INSTALLER_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "installer")
DATA = os.path.join(INSTALLER_DIR, "legacy-40r12.txt")
FILE_ATTR = 0o100666 << 16  # the mode Python 2.7 on Windows recorded for files
LF_MANIFEST = "uac-lf.exe.manifest"

active = False
sha256 = None
pe_stamp = None
classes = {}  # entry -> bytes
times = {}    # installer entry -> date_time
inner = []    # version.jar date_times, in entry order
_in_order = {}  # id(ZipFile) -> iterator over date_times
_deflate = None
java = None  # (javac argv, java argv), set by build.py

def _date_time(text):
    return tuple(int(n) for n in text.replace("-", " ").replace("T", " ").replace(":", " ").split())

def load(installer_id):
    global active, sha256, pe_stamp
    if not os.path.exists(DATA):
        return
    legend = {}
    with open(DATA, "r") as fh:
        for line in fh:
            f = line.split()
            if not f or f[0].startswith("#"):
                continue
            if f[0] == "installer" and f[1] != installer_id:
                return
            elif f[0] == "sha256":
                sha256 = f[1]
            elif f[0] == "pestamp":
                pe_stamp = int(f[1], 16)
            elif f[0] == "class":
                data = base64.b64decode(f[3])
                if hashlib.sha1(data).hexdigest() != f[2]:
                    raise Exception("%s: corrupt entry %s" % (DATA, f[1]))
                classes[f[1]] = data
            elif f[0] == "time":
                times[f[1]] = _date_time(f[2])
            elif f[0] == "legend":
                legend.update((k, _date_time(v)) for k, v in (kv.split("=") for kv in f[1:]))
            elif f[0] == "inner":
                inner.extend(legend[c] for c in f[1])
    active = True
    print("Reproducing the official %s" % installer_id)
    _start(*java)

class _ClassicDeflater(object):
    # Python 3.14 on Windows ships zlib-ng, whose deflate streams differ from classic zlib's
    def __init__(self):
        self.chunks = []
    def compress(self, data):
        self.chunks.append(bytes(data))
        return b""
    def flush(self):
        data = b"".join(self.chunks)
        _deflate.stdin.write(struct.pack(">i", len(data)) + data)
        _deflate.stdin.flush()
        n, = struct.unpack(">i", _deflate.stdout.read(4))
        return _deflate.stdout.read(n)

def _start(javac, java):
    """Compress deflated zip entries with the JDK's classic zlib until stop()."""
    global _deflate
    classes_dir = tempfile.mkdtemp()
    subprocess.check_call(javac + ["-d", classes_dir, os.path.join(INSTALLER_DIR, "RawDeflate.java")])
    _deflate = subprocess.Popen(java + ["-cp", classes_dir, "RawDeflate"], stdin=subprocess.PIPE, stdout=subprocess.PIPE)
    _deflate.classes_dir = classes_dir
    get_compressor = zipfile._get_compressor
    zipfile._get_compressor = lambda t, level=None: _ClassicDeflater() if t == zipfile.ZIP_DEFLATED else get_compressor(t, level)
    zipfile._get_compressor.original = get_compressor

def stop():
    if _deflate is None:
        return
    zipfile._get_compressor = zipfile._get_compressor.original
    _deflate.stdin.write(struct.pack(">i", -1))
    _deflate.stdin.close()
    _deflate.wait()
    shutil.rmtree(_deflate.classes_dir)

def pin_in_order(zf):
    """Give zf's entries the version.jar timestamps, in the order they are written."""
    if active:
        _in_order[id(zf)] = iter(inner)

def _pinned(zf, name):
    if id(zf) in _in_order:
        return next(_in_order[id(zf)], None)
    return times.get(name)

def write(zf, path, arcname):
    """zf.write(path, arcname); path None writes the stale class arcname."""
    if not active:
        return zf.write(path, arcname)
    if path is None:
        info, data = zipfile.ZipInfo(arcname), classes[arcname]
    else:
        info = zipfile.ZipInfo.from_file(path, arcname)
        with open(path, "rb") as fh:
            data = fh.read()
    info.date_time = _pinned(zf, info.filename) or info.date_time
    info.compress_type = zf.compression
    info.external_attr = FILE_ATTR
    zf.writestr(info, data)

def writestr(zf, name, data):
    when = _pinned(zf, name) if active else None
    if when is None:
        return zf.writestr(name, data)
    info = zipfile.ZipInfo(name, when)
    info.compress_type = zf.compression
    info.external_attr = 0o600 << 16
    zf.writestr(info, data)

def with_stale_classes(entries):
    """Add the stale classes to [(path, arcname)] in the directory walk order they had."""
    if not active:
        return entries
    entries = [(p, os.path.normpath(a).replace(os.sep, "/")) for p, a in entries]
    written = set(a for _, a in entries)
    entries += [(None, name) for name in classes if name not in written]
    def walk_key(entry):
        parts = entry[1].upper().split("/")
        return parts[:-1], parts[-1]
    return sorted(entries, key=walk_key)

def launch4j_config(config, launch4j_dir):
    """Point launch4j at an LF copy of the manifest, as the official checkout had."""
    if not active:
        return config
    manifest = config.split("<manifest>")[1].split("</manifest>")[0]
    with open(os.path.join(launch4j_dir, manifest), "rb") as fh:
        text = fh.read().replace(b"\r\n", b"\n")
    with open(os.path.join(launch4j_dir, LF_MANIFEST), "wb") as fh:
        fh.write(text)
    return config.replace(manifest, LF_MANIFEST)

def finish_exe(exe, jar, launch4j_dir):
    """Give the exe the official link time and check it against the official sha256."""
    if not active:
        return
    os.unlink(os.path.join(launch4j_dir, LF_MANIFEST))
    with open(exe, "rb") as fh:
        b = bytearray(fh.read())
    head = len(b) - os.path.getsize(jar)  # launch4j appends the jar to its header
    pe = struct.unpack_from("<I", b, 0x3c)[0]
    b[:head] = bytes(b[:head]).replace(b[pe + 8:pe + 12], struct.pack("<I", pe_stamp))
    # PE checksum of the header launch4j linked, before the jar was appended
    struct.pack_into("<I", b, pe + 88, 0)
    s = 0
    for (word,) in struct.iter_unpack("<H", bytes(b[:head])):
        s += word
        s = (s & 0xffff) + (s >> 16)
    struct.pack_into("<I", b, pe + 88, s + head)
    with open(exe, "wb") as fh:
        fh.write(b)
    ours = hashlib.sha256(b).hexdigest()
    if ours == sha256:
        print("Installer exe matches the official release: sha256 %s" % ours)
    else:
        print("!! Installer exe differs from the official release: sha256 %s, official %s !!" % (ours, sha256))
