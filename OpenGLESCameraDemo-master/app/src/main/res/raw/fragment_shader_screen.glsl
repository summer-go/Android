#extension GL_OES_EGL_image_external : require //申明使用扩展纹理
precision mediump float;//精度 为float
varying vec2 v_texPo;//纹理位置  接收于vertex_shader
uniform sampler2D  sTexture;
uniform float iTime;

// from https://www.shadertoy.com/view/7dXXWs


#define PI 3.1415926535897932384626433832795

float rand(vec2 c){
    return fract(sin(dot(c.xy, vec2(12.9898, 78.233))) * 43758.5453);
}
float noise1(vec2 p, float freq){
//    float unit = iResolution.x/freq;
    float unit = v_texPo.x * 720.0/freq;
    vec2 ij = floor(p/unit);
    vec2 xy = mod(p, unit)/unit;
    //xy = 3.*xy*xy-2.*xy*xy*xy;
    xy = .5*(1.-cos(PI*xy));
    float a = rand((ij+vec2(0., 0.)));
    float b = rand((ij+vec2(1., 0.)));
    float c = rand((ij+vec2(0., 1.)));
    float d = rand((ij+vec2(1., 1.)));
    float x1 = mix(a, b, xy.x);
    float x2 = mix(c, d, xy.x);
    return mix(x1, x2, xy.y);
}

float pNoise1(vec2 p, int res){
    float persistance = .5;
    float n = 0.;
    float normK = 0.;
    float f = 4.;
    float amp = 1.;
    int iCount = 0;
    for (int i = 0; i<50; i++){
        n+=amp*noise1(p, f);
        f*=2.;
        normK+=amp;
        amp*=persistance;
        if (iCount == res) break;
        iCount++;
    }
    float nf = n/normK;
    return nf*nf*nf*nf;
}

void mainImage(vec4 fragColor, vec2 fragCoord)
{

    float radius = 0.000001;
    float internalRadius = 0.;
    float amount = 2.2;
    radius += abs(mod(iTime, amount+amount)-amount);
    internalRadius += abs(mod(iTime, amount+2.)-2.);
    internalRadius = radius / 2.;
    float radiusInterval = 14.;
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord;

    vec4 video = texture2D(sTexture, uv);
    vec2 circleMid = vec2(0.5, 0.5);

    float norm = 720.0/1280.0;
//    float norm = 1.0;
    vec2 circ = vec2((uv.x - circleMid.x)*norm, uv.y - circleMid.y);

    float dist = length(circ);


    for (float i=0.;i<radiusInterval;++i)
    {

        if (dist <= radius && internalRadius <= dist) {
            circ.x *= (rand(vec2(iTime, circ.x))/1.);

            video = texture2D(sTexture, circ);
        }
        radius += 1./radiusInterval;
        internalRadius += 1./radiusInterval;
    }

    // Output to screen
    gl_FragColor = video;
}

void main() {
    gl_FragColor = texture2D(sTexture, v_texPo);

    //     gl_FragColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    mainImage(gl_FragColor, v_texPo);
}