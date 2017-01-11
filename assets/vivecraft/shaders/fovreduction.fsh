#version 120

uniform sampler2D tex0;
uniform float circle_radius;
uniform float border;

uniform float water;
uniform float portal;

uniform float portaltime;
uniform float redalpha;
uniform float blackalpha;

const vec4 black = vec4(0, 0, 0, 1.0);
const float pi = 3.14159265;

void main(){

	vec4 bkg_color = texture2D(tex0,gl_TexCoord[0].st); 
    
	if(portal > 0){ //swirly whirly
		float ts = gl_TexCoord[0].s;
		vec2 mod_texcoord = gl_TexCoord[0].st + vec2(water*.005*cos(portaltime + 20*ts*pi), water*.005*sin(portaltime + 30*ts*pi));
		bkg_color = texture2D(tex0, mod_texcoord);
	}

	if(water > 0){ //goobly woobly
		float ts = gl_TexCoord[0].s;
		vec2 mod_texcoord = gl_TexCoord[0].st + vec2(0, water*.0015*sin(portaltime + 15*ts*pi));
		bkg_color = texture2D(tex0, mod_texcoord);
	}
	
	if(redalpha > 0){ //ouchy wouchy
		vec4 red = vec4(bkg_color.r, 0, 0, 1.0);
		bkg_color  = mix(bkg_color, red, redalpha);
	}

	if(blackalpha > 0){ //spooky wooky
		bkg_color  = mix(bkg_color, black, blackalpha);
	}

	if(circle_radius < 0.8){ //arfy barfy
		vec2 circle_center = vec2(0.5, 0.5);
		vec2 uv = gl_TexCoord[0].xy; 
		uv -= circle_center; 
		float dist =  sqrt(dot(uv, uv)); 
		float t = 1.0 + smoothstep(circle_radius, circle_radius+10, dist) - smoothstep(circle_radius-border, circle_radius, dist);
		bkg_color  = mix(black, bkg_color,t);
	}	
	
	gl_FragColor = bkg_color;
	
}