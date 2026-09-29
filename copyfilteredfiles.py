import os
import shutil
from optparse import OptionParser

def create_relative_file_list(dir_path):

    file_list = []
    for root, _, files in os.walk(dir_path):
        for file in files:
            relative_file_path = os.path.join(os.path.relpath(root, dir_path), file).replace('.patch', '')
            print('  ' + relative_file_path)
            file_list.append(relative_file_path)

    print('%d File(s)' % len(file_list))
    return file_list


def copy_filtered_files(filter_dir, sourceDir, destDir, cleanDest):

    if cleanDest:
        # clean dest dir
        print('Cleaning dest: %s' % destDir)
        shutil.rmtree(destDir, ignore_errors=True)

    # create filter list
    print('Filter list:')
    relative_filter = create_relative_file_list(filter_dir)
    

    # walk the (relative path) file filter list, copy files
    print('Copy files:')
    for file in relative_filter:

        # create absolute paths
        src_file = os.path.join(sourceDir, file)
        dest_file = os.path.join(destDir, file)

        # copy 'em
        if os.path.exists(src_file):
            dest_dir = os.path.dirname(dest_file)
            if not os.path.exists(dest_dir):
                os.makedirs(dest_dir)
            print('  %s -> %s' % (src_file, dest_file))
            shutil.copyfile(src_file, dest_file)


if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option('-f', '--filter',dest='filterDir',action='store',default=None,help='Directory to create file filter from')
    parser.add_option('-s', '--source',dest='sourceDir',action='store',default=None,help='Source directory to copy (filtered) files from')
    parser.add_option('-d', '--dest',dest='destDir',action='store',default=None,help='Destination directory to copy (filtered) files to')
    parser.add_option('-c', '--cleandest',dest='cleanDest',action='store_true',default=False,help='Clean destination directory first')
    options,_= parser.parse_args()

    filterDir = options.filterDir
    sourceDir = options.sourceDir
    destDir = options.destDir
    cleanDest = options.cleanDest

    copy_filtered_files(filterDir, sourceDir, destDir, cleanDest)