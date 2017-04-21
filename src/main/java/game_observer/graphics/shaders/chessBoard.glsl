/*** VERTEX SHADER ***/
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


/*** FRAGMENT SHADER ***/
#version 330 core

uniform bool useTexture;
uniform sampler2D texSampler;

in vec3 vertexNormal;
in vec2 texCoord;

out vec3 color;

const vec3 lightDirection = normalize(vec3(0, 1, -1));
const vec3 lightColor = vec3(1.0, 1.0, 1.0);
void main()
{
	float cosTheta = clamp(dot(vertexNormal, lightDirection), 0, 1);

	if (useTexture)
	{
		vec3 materialDiffuseColor = texture(texSampler, texCoord).rrr;
		vec3 materialAmbientColor = vec3(0.1, 0.1, 0.1) * materialDiffuseColor;

		color = materialAmbientColor
			  + materialDiffuseColor * lightColor * cosTheta;
	} else
	{
		vec3 materialAmbientColor = vec3(0.1, 0.1, 0.1);

		color = materialAmbientColor + lightColor * cosTheta;
	}
}
