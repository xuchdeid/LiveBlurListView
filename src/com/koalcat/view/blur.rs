#pragma version(1)
#pragma rs java_package_name(com.koalcat.blurdemo)
#include "rs_graphics.rsh"
#include "rs_cl.rsh"

typedef struct ConvolveParams_s {
    float fp[104];
    uint32_t ip[104];
    float radius;
    uint32_t iradius;
} ConvolveParams;

ConvolveParams *cp;
float radius;
const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

void setup() {
    float e = 2.718281828459045f;
    float pi = 3.1415926535897932f;
    float sigma = 0.4f * radius + 0.6f;
    
    float coeff1 = 1.0f / (sqrt(2.0f * pi) * sigma);
    float coeff2 = - 1.0f / (2.0f * sigma * sigma);

/*    float normalizeFactor = 0.0f;
    float floatR = 0.0f;
    int r;
    cp->iradius = (float)ceil(cp->radius) + 0.5f;
    for (r = -cp->iradius; r <= cp->iradius; r ++) {
        floatR = (float)r;
        cp->fp[r + cp->iradius] = coeff1 * pow(e, floatR * floatR * coeff2);
        normalizeFactor += cp->fp[r + cp->iradius];
    }

    normalizeFactor = 1.0f / normalizeFactor;
    for (r = -cp->iradius; r <= cp->iradius; r ++) {
        cp->fp[r + cp->iradius] *= normalizeFactor;
        cp->ip[r + cp->iradius] = (cp->ip[r + cp->iradius] * 32768);
    }
    */
}

void init() {

    radius = 5;
    setup();

}

void root(const uchar4 *v_in, uchar4 *v_out, uint32_t x, uint32_t y) {
    float4 f4 = rsUnpackColor8888(*v_in);
    float3 mono = dot(f4.rgb, gMonoMult);
    *v_out = rsPackColorTo8888(mono);
}