
#define numDirectionalLights 2
#define bubblesTotalCount %d
#define wavesTotalCount %d

attribute vec3 a_position;
uniform mat4 u_projViewTrans;

uniform mat3 u_normalMatrix;
varying vec3 v_normal;

uniform mat4 u_worldTrans;

varying vec3 v_lightDiffuse;
uniform vec3 u_ambientCubemap[6];

struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];

uniform vec4 u_bubbles[bubblesTotalCount];
uniform int u_count_bubbles;

uniform vec3 u_waves[wavesTotalCount];
uniform int u_count_waves;

uniform float u_camera_far;

const float u_maxX_p = 2500.0;
const float u_maxY_p = 2500.0;

float PI = 3.14159265358979323846264;
float half_maxX = u_maxX_p / 2.0;
float half_maxY = u_maxY_p / 2.0;

const float maxDiffLen = 1770.0;
float maxDiffQuad = maxDiffLen * maxDiffLen;

varying float v_fog_fraction;
uniform vec3 u_camera;



float pow6(float val){
	float r = val * val * val;
	return r * r;
}

float get_fog_fraction(vec3 pos){
	float len = length(vec3(pos.x - u_camera.x, pos.y - u_camera.y, pos.z - u_camera.z));
	return min(1.0, pow6(len/u_camera_far));
}
	
vec3 calcNormal(vec2 lVector, float maxZ, float amplitude, float len){
	vec3 norm = vec3(lVector.x, lVector.y, 0.0);
	float k = -1.0 / (2.0 * (amplitude / maxDiffQuad) * (len - maxDiffLen));
	norm.z = abs(k * length(norm));
	return normalize(norm);
}

mat3 rotationMatrix3(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat3(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c           );
}

float rand(vec2 co){
  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec3 makeHNormal(vec3 normal, float len){
	vec3 h_normal = vec3(normal.x, normal.y, 0.0);
	mat3 rmat = rotationMatrix3(normal, rand(vec2(normal.x * len, normal.z * len * 1.07)) * PI);
	h_normal.xyz = cross(normal, h_normal);
	h_normal.xyz = rmat * h_normal;
	return h_normal;
}

vec2 findLensVector(vec3 center, vec3 pos){
	vec2 lV = vec2(pos.x - center.x, pos.y - center.y);
	if(lV.x > half_maxX){
		lV.x = lV.x - u_maxX_p;
	}
	if(lV.x < -half_maxX){
		lV.x = lV.x + u_maxX_p;
	}
	if(lV.y > half_maxY){
		lV.y = lV.y - u_maxY_p;
	}
	if(lV.y < -half_maxY){
		lV.y = lV.y + u_maxY_p;
	}
	return lV;
}

void main(){

	float max_Z = 0.0;
	int maxK = -1;
	float lenMax = 0.0;
	vec2 lVector;
	for(int k=0; k<u_count_waves; k++){
		vec2 lenVector = findLensVector(u_waves[k], a_position);
		float curLen = length(lenVector);
		float df = curLen - maxDiffLen;
		float curZ = df * df * u_waves[k].z / maxDiffQuad;
		if(curZ > max_Z){
			max_Z = curZ;
			maxK = k;
			lenMax = curLen;
			lVector = lenVector;
		}
	}

	vec4 pos = u_worldTrans * vec4(a_position.x, a_position.y, max_Z, 1.0);
	v_fog_fraction = get_fog_fraction(pos.xyz);
	gl_Position = u_projViewTrans * pos;
	vec3 normal = normalize(u_normalMatrix * calcNormal(lVector, max_Z, u_waves[maxK].z, lenMax));
	
	float diff = 0.0;
	float len = 0.0;
	for(int j=0; j<u_count_bubbles; j++){
		len = length(pos.xy - u_bubbles[j].xy);
		if( len < u_bubbles[j].w ){
			diff = diff + u_bubbles[j].z * ( 1.0 - len / u_bubbles[j].w);
		}
	}
	if(diff > 0.0){
		vec3 h_normal = makeHNormal(normal, len);
		normal = rotationMatrix3(h_normal, rand(vec2(h_normal.z * len, h_normal.y * len * 1.05)) * diff) * normal;	
	}
	
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