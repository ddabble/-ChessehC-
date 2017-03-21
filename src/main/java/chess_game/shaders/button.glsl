/*** VERTEX SHADER ***/
#version 330 core

layout(location = 0) in vec4 coord;
layout(location = 1) in vec2 tex;

out vec2 texCoord;

void main()
{
	gl_Position = coord;
	texCoord = tex;
}


/*** FRAGMENT SHADER ***/
#version 330 core

uniform sampler2D texSampler;
uniform bool background;
uniform vec3 backgroundColor;

in vec2 texCoord;

out vec4 color;

void main()
{
	if (background)
		color = vec4(backgroundColor, 1);
	else
		color = texture(texSampler, texCoord);
}
