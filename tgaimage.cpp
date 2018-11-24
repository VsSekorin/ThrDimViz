#include <iostream>
#include <fstream>
#include <string.h>
#include <time.h>
#include <math.h>
#include "tgaimage.h"

TGAImage::TGAImage() : data(NULL), width(0), height(0), bytespp(0) {
    //standard useless constructor. Or not? I wanna delete this.
    //And NULL. [BibleThump]
}

TGAImage::TGAImage(int width, int height, int bpp) : data(NULL), width(width), height(height), bytespp(bpp) {
    const auto nbytes = static_cast<unsigned long>(this->width * this->height * this->bytespp);
    this->data = new unsigned char[nbytes];
    memcpy(this->data, 0, nbytes); //Clion think that I should use nullptr instead of 0.
}

TGAImage::TGAImage(const TGAImage &image) : data(NULL), width(image.width), height(image.height),
                                            bytespp(image.bytespp) {
    const auto nbytes = static_cast<unsigned long>(this->width * this->height * this->bytespp);
    this->data = new unsigned char[nbytes];
    memcpy(this->data, image.data, nbytes);
}

TGAImage::~TGAImage() {
    if (this->data) delete[] data; //Clion suggest me remove `if`, just `delete[] data`.
}

TGAColor TGAImage::get(int x, int y) {
    if (!data || x < 0 || y < 0 || x >= width || y >= height) {
        return TGAColor();
    }
    return TGAColor(data + (x + y * width) * bytespp, static_cast<unsigned char>(bytespp));
}

bool TGAImage::set(int x, int y, TGAColor color) {
    if (!data || x < 0 || y < 0 || x >= width || y >= height) {
        return false;
    }
    memcpy(data + (x + y * width) * bytespp, color.bgra, static_cast<size_t>(bytespp));
    return true;
}

bool TGAImage::flip_vertically() {
    if (!data) return false;
    auto bytes_per_line = static_cast<unsigned long>(width * bytespp);
    auto *line = new unsigned char[bytes_per_line];
    int half = height >> 1;
    for (int j = 0; j < half; j++) {
        unsigned long l1 = j * bytes_per_line;
        unsigned long l2 = (height - 1 - j) * bytes_per_line;
        memmove((void *) line, (void *) (data + l1), bytes_per_line);
        memmove((void *) (data + l1), (void *) (data + l2), bytes_per_line);
        memmove((void *) (data + l2), (void *) line, bytes_per_line);
    }
    delete[] line;
    return true;
}


/////////////////////
//
//   IN ?????????????
//
////////////////////
bool TGAImage::save(const char *filename, bool rle) { //rle???
    auto pixelcount = static_cast<unsigned long>(width * height);
    unsigned long currentpixel = 0;
    unsigned long currentbyte = 0;
    TGAColor colorbuffer;
    do {
        unsigned char chunkheader = 0;
        chunkheader = in.get();
        if (!in.good()) {
            std::cerr << "an error occured while reading the data\n";
            return false;
        }
        if (chunkheader < 128) {
            chunkheader++;
            for (int i = 0; i < chunkheader; i++) {
                in.read((char *) colorbuffer.bgra, bytespp);
                if (!in.good()) {
                    std::cerr << "an error occured while reading the header\n";
                    return false;
                }
                for (int t = 0; t < bytespp; t++)
                    data[currentbyte++] = colorbuffer.bgra[t];
                currentpixel++;
                if (currentpixel > pixelcount) {
                    std::cerr << "Too many pixels read\n";
                    return false;
                }
            }
        } else {
            chunkheader -= 127;
            in.read((char *) colorbuffer.bgra, bytespp);
            if (!in.good()) {
                std::cerr << "an error occured while reading the header\n";
                return false;
            }
            for (int i = 0; i < chunkheader; i++) {
                for (int t = 0; t < bytespp; t++)
                    data[currentbyte++] = colorbuffer.bgra[t];
                currentpixel++;
                if (currentpixel > pixelcount) {
                    std::cerr << "Too many pixels read\n";
                    return false;
                }
            }
        }
    } while (currentpixel < pixelcount);
    return true;
}

