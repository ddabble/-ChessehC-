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
uniform ivec2 textureSize;
uniform bool blur;

in vec2 texCoord;

out vec3 color;

const int BLUR_RADIUS = 3;
const int BLUR_AREA = (BLUR_RADIUS * 2 + 1) * (BLUR_RADIUS * 2 + 1);
void main()
{
	if (blur)
	{
		float texelWidth = 1.0 / textureSize.x;
		float texelHeight = 1.0 / textureSize.y;

		vec3 blurColor = vec3(0);

		for (int y = -BLUR_RADIUS; y <= BLUR_RADIUS; y++)
		{
			for (int x = -BLUR_RADIUS; x <= BLUR_RADIUS; x++)
				blurColor += texture(texSampler, vec2(texCoord.x + x * texelWidth, texCoord.y + y * texelHeight)).rgb;
		}

		color = blurColor / BLUR_AREA;
	} else
		color = texture(texSampler, texCoord).rgb;
}
