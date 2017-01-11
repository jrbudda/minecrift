#version 120

 uniform float texelWidthOffset;
 uniform float texelHeightOffset;

 varying vec2 centerTextureCoordinate;
 varying vec2 oneStepLeftTextureCoordinate;
 varying vec2 twoStepsLeftTextureCoordinate;
 varying vec2 threeStepsLeftTextureCoordinate;
 varying vec2 fourStepsLeftTextureCoordinate;
 varying vec2 oneStepRightTextureCoordinate;
 varying vec2 twoStepsRightTextureCoordinate;
 varying vec2 threeStepsRightTextureCoordinate;
 varying vec2 fourStepsRightTextureCoordinate;

 void main()
 {
	 gl_Position = ftransform();

	 vec2 firstOffset = vec2(texelWidthOffset, texelHeightOffset);
	 vec2 secondOffset = vec2(2.0 * texelWidthOffset, 2.0 * texelHeightOffset);
	 vec2 thirdOffset = vec2(3.0 * texelWidthOffset, 3.0 * texelHeightOffset);
	 vec2 fourthOffset = vec2(4.0 * texelWidthOffset, 4.0 * texelHeightOffset);

	 vec2 textCoord = gl_MultiTexCoord0.xy;
	 centerTextureCoordinate = textCoord;
	 oneStepLeftTextureCoordinate = textCoord - firstOffset;
	 twoStepsLeftTextureCoordinate = textCoord - secondOffset;
	 threeStepsLeftTextureCoordinate = textCoord - thirdOffset;
	 fourStepsLeftTextureCoordinate = textCoord - fourthOffset;
	 oneStepRightTextureCoordinate = textCoord + firstOffset;
	 twoStepsRightTextureCoordinate = textCoord + secondOffset;
	 threeStepsRightTextureCoordinate = textCoord + thirdOffset;
	 fourStepsRightTextureCoordinate = textCoord + fourthOffset;
 }