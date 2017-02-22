#version 330 core

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

layout(location = 0) in vec4 coord;
layout(location = 1) in vec4 normal;

out vec3 position_worldspace;
out vec3 vertexNormal_cameraspace;
out vec3 eyeDirection_cameraspace;
out vec3 lightDirection_cameraspace;

const vec3 lightPosition_worldspace = vec3(0, 1000, 0);
void main()
{
	gl_Position = projection * view * model * coord;

	position_worldspace = (model * coord).xyz;

	vertexNormal_cameraspace = normal.xyz;

	vec3 vertexPosition_cameraspace = (view * model * coord).xyz;
    eyeDirection_cameraspace = vec3(0, 0, 0) - vertexPosition_cameraspace;

    vec3 lightPosition_cameraspace = (view * vec4(lightPosition_worldspace, 1)).xyz;
    lightDirection_cameraspace = lightPosition_cameraspace + eyeDirection_cameraspace;
}
