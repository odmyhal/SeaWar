#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec3 v_normal;
uniform vec4 u_diffuseColor;

varying vec3 v_lightDiffuse;
varying float v_fog_fraction;

const vec4 fog_color = vec4(0.9, 0.9, 1, 0.5);

void main(){
	vec3 normal = v_normal;
	
	vec4 diffuse = u_diffuseColor;
	vec3 color = (diffuse.rgb * v_lightDiffuse);
	gl_FragColor.rgb = mix(color, fog_color.xyz, v_fog_fraction);
//	gl_FragColor.rgb = mix(color, fog_color, 0.0009);
//	gl_FragColor.rgb = color;
	gl_FragColor.a = mix(0.9, fog_color.w, v_fog_fraction);
}