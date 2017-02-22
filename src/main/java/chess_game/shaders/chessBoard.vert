#version 330 core

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

layout(location = 0) in vec4 coord;
layout(location = 1) in vec2 tex;
layout(location = 2) in vec4 normal;

out vec3 vertexNormal;
out vec2 texCoord;

void main()
{
	mat4 MVP = projection * view * model;

	gl_Position = MVP * coord;
	vertexNormal = normal.xyz;

	texCoord = tex;
}
