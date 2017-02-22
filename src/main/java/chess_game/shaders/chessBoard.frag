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
