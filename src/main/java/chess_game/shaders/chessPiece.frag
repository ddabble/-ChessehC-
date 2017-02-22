#version 330 core

uniform vec3 materialDiffuseColor;

in vec3 position_worldspace;
in vec3 vertexNormal_cameraspace;
in vec3 eyeDirection_cameraspace;
in vec3 lightDirection_cameraspace;

out vec3 color;

//const vec3 lightDirection = normalize(vec3(0, 1, -1));
const vec3 lightColor = vec3(1.0, 1.0, 1.0);
const vec3 materialSpecularColor = vec3(0.3, 0.3, 0.3);
const vec3 lightPosition_worldspace = vec3(0, 1000, 0);
void main()
{
	vec3 l = normalize(lightDirection_cameraspace);

	float cosTheta = clamp(dot(vertexNormal_cameraspace, l), 0, 1);

	vec3 materialAmbientColor = vec3(0.1, 0.1, 0.1) * materialDiffuseColor;

	float distance = length(lightPosition_worldspace - position_worldspace);

	vec3 E = normalize(eyeDirection_cameraspace);
	vec3 R = reflect(-l, vertexNormal_cameraspace);
	float cosAlpha = clamp(dot(E, R), 0, 1);

	color = materialAmbientColor
		  + materialDiffuseColor * lightColor * cosTheta /*/ (distance * distance)*/
		  + materialSpecularColor * lightColor * pow(cosAlpha, 5) /*/ (distance * distance)*/;
}
