#ifndef THRDIMVIZ_TGAIMAGE_H
#define THRDIMVIZ_TGAIMAGE_H

#include <fstream>

#pragma pack(push, 1)
struct TGA_Header {
    char idlength;
    char colormaptype;
    char datatypecode;
    short colormaporigin;
    short colormaplength;
    char colormapdepth;
    short x_origin;
    short y_origin;
    short width;
    short height;
    char bitsperpixel;
    char imagedescriptor;
};
#pragma pack(pop)

struct TGAColor {
    unsigned char bgra[4];
    unsigned char bytespp;

    TGAColor() : bgra(), bytespp(1) {
        for (unsigned char &i : bgra) {
            i = 0;
        }
    }

    TGAColor(unsigned char R, unsigned char G, unsigned char B, unsigned char A = 255) : bgra(), bytespp(4) {
        bgra[0] = B;
        bgra[1] = G;
        bgra[2] = R;
        bgra[3] = A;
    }

    TGAColor(unsigned char v) : bgra(), bytespp(1) {
        for (unsigned char &i : bgra) {
            i = 0;
        }
        bgra[0] = v;
    }

    TGAColor(const unsigned char *p, unsigned char bpp) : bgra(), bytespp(bpp) {
        for (int i = 0; i < (int) bpp; i++) {
            bgra[i] = p[i];
        }
        for (int i = bpp; i < 4; i++) {
            bgra[i] = 0;
        }
    }

    unsigned char &operator[](const int i) {
        return bgra[i];
    }

    TGAColor operator*(float intensity) const {
        TGAColor res = *this;
        intensity = (intensity > 1.f ? 1.f : (intensity < 0.f ? 0.f : intensity));
        for (int i = 0; i < 4; i++) {
            res.bgra[i] = static_cast<unsigned char>(bgra[i] * intensity);
        }
        return res;
    }
};

class TGAImage {
private:
    unsigned char *data;
    int width;
    int height;
    int bytespp;

    bool load_rle_data(std::ifstream &in);
    bool unload_rle_data(std::ofstream &out);
    bool flip_horizontally();

public:
    enum Format {
        GRAYSCALE = 1, RGB = 3, RGBA = 4
    };

    TGAImage();
    TGAImage(int width, int height, int bpp);
    TGAImage(const TGAImage &image);

    TGAColor get(int x, int y);
    bool set(int x, int y, TGAColor color);
    bool flip_vertically();
    bool read(const char *filename);
    bool write(const char *filename, bool rle = true);

    ~TGAImage();
};

#endif //THRDIMVIZ_TGAIMAGE_H
