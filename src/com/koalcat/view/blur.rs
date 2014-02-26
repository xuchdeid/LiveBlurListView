#pragma version(1)
#pragma rs java_package_name(com.koalcat.blurdemo)

#include "rs_graphics.rsh"
#include "rs_cl.rsh"
#include "rs_debug.rsh"

typedef struct ConvolveParams_s {
    float fp[104];
    uint32_t ip[104];
    float radius;
    uint32_t iradius;
} ConvolveParams;

ConvolveParams *cp;
float radius;
float width;
float height;


const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

void setup() {

    float e = 2.718281828459045f;
    float pi = 3.1415926535897932f;
    float sigma = 0.4f * radius + 0.6f;
    
    float coeff1 = 1.0f / (sqrt(2.0f * pi) * sigma);
    float coeff2 = - 1.0f / (2.0f * sigma * sigma);

    float normalizeFactor = 0.0f;
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
    
}

void init() {
    radius = 5;
    width = 0;
    height = 0;
}

void root(const uchar4 *v_in, uchar4 *v_out, uint32_t x, uint32_t y) {

    /*uchar4 out1 = v_in[0];
    out1.r = 255 - out1.r;
    out1.g = 255 - out1.g;
    out1.b = 255 - out1.b;
    
    *v_out = out1;
    
    return;
    */
    
    if (x == 0 || y == 0 || x == width || y == height) {
        *v_out = v_in[0];
	    return;
	}
    
    uchar4 out;
    int R = 0, G = 0, B = 0;
    
    for (int g = -1; g <= 1; g ++) {
	    for (int r = -1; r <= 1; r ++) {
	        if (r == 0 && g == 0) continue;
	        int p = r + g * 3;
	        
	        out = v_in[p];
	        R += out.r*0.125f;
	        G += out.g*0.125f;
	        B += out.b*0.125f;
	    }
    }
    out.r = R;
    out.g = G;
    out.b = B;
    
    *v_out = out;

}
/*
uchar4 __attribute__((kernel)) invert(uchar4 in, uint32_t x, uint32_t y) {
  uchar4 out = in;
  out.r = 255 - in.r;
  out.g = 255 - in.g;
  out.b = 255 - in.b;
  return out;
}
*/