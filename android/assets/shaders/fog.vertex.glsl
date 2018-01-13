attribute vec3 a_position;
uniform mat4 u_projViewTrans;

attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
varying vec3 v_normal;

uniform mat4 u_worldTrans;

varying vec3 v_lightDiffuse;
uniform vec3 u_ambientCubemap[6];

varying float v_fog_fraction;
uniform vec3 u_camera;

struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];

uniform float u_camera_far;

float pow6(float val){
	float r = val * val * val;
	return r * r;
}


float get_fog_fraction(vec3 pos){
	float len = length(vec3(pos.x - u_camera.x, pos.y - u_camera.y, pos.z - u_camera.z));
	return min(1.0, pow6(len/u_camera_far));
}

void main(){
	
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	v_fog_fraction = get_fog_fraction(pos.xyz);
	gl_Position = u_projViewTrans * pos;
	
	vec3 normal = normalize(u_normalMatrix * a_normal);
	v_normal = normal;
	
	vec3 ambientLight = vec3(0.0);
	vec3 squaredNormal = normal * normal;
			vec3 isPositive  = step(0.0, normal);
			ambientLight += squaredNormal.x * mix(u_ambientCubemap[0], u_ambientCubemap[1], isPositive.x) +
					squaredNormal.y * mix(u_ambientCubemap[2], u_ambientCubemap[3], isPositive.y) +
					squaredNormal.z * mix(u_ambientCubemap[4], u_ambientCubemap[5], isPositive.z);
	v_lightDiffuse = ambientLight;
	for (int i = 0; i < numDirectionalLights; i++) {
				vec3 lightDir = -u_dirLights[i].direction;
				float NdotL = clamp(dot(normal, lightDir), 0.0, 1.0);
				vec3 value = u_dirLights[i].color * NdotL;
				v_lightDiffuse += value;
	}		
}