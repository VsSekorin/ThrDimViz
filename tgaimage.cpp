#include <iostream>
#include <fstream>
#include <string.h>
#include <time.h>
#include <math.h>
#include "tgaimage.h"

TGAImage::TGAImage() : data(NULL), width(0), height(0), bytespp(0) {
}

TGAImage::TGAImage(int width, int height, int bpp) : data(NULL), width(width), height(height), bytespp(bpp) {
    const auto nbytes = static_cast<unsigned long>(this->width * this->height * this->bytespp);
    this->data = new unsigned char[nbytes];
    memcpy(this->data, 0, nbytes);
}

TGAImage::TGAImage(const TGAImage &image) : data(NULL), width(image.width), height(image.height),
                                            bytespp(image.bytespp) {
    const auto nbytes = static_cast<unsigned long>(this->width * this->height * this->bytespp);
    this->data = new unsigned char[nbytes];
    memcpy(this->data, image.data, nbytes);
}

TGAImage::~TGAImage() {
    if (this->data) delete[] data;
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

bool TGAImage::flip_horizontally() {
    if (!data) return false;
    int half = width >> 1;
    for (int i = 0; i < half; i++) {
        for (int j = 0; j < height; j++) {
            TGAColor c1 = get(i, j);
            TGAColor c2 = get(width - 1 - i, j);
            set(i, j, c2);
            set(width - 1 - i, j, c1);
        }
    }
    return true;
}

bool TGAImage::read(const char *filename) {
    if (data) delete[] data;
    data = nullptr;
    std::ifstream in;
    in.open(filename, std::ios::binary);
    if (!in.is_open()) {
        std::cerr << "can't open file " << filename << "\n";
        in.close();
        return false;
    }
    TGA_Header header{};
    in.read((char *) &header, sizeof(header));
    if (!in.good()) {
        in.close();
        std::cerr << "an error occured while reading the header\n";
        return false;
    }
    width = header.width;
    height = header.height;
    bytespp = header.bitsperpixel >> 3;
    if (width <= 0 || height <= 0 || (bytespp != GRAYSCALE && bytespp != RGB && bytespp != RGBA)) {
        in.close();
        std::cerr << "bad bpp (or width/height) value\n";
        return false;
    }
    auto nbytes = static_cast<unsigned long>(bytespp * width * height);
    data = new unsigned char[nbytes];
    if (3 == header.datatypecode || 2 == header.datatypecode) {
        in.read((char *) data, nbytes);
        if (!in.good()) {
            in.close();
            std::cerr << "an error occured while reading the data\n";
            return false;
        }
    } else if (10 == header.datatypecode || 11 == header.datatypecode) {
        if (!load_rle_data(in)) {
            in.close();
            std::cerr << "an error occured while reading the data\n";
            return false;
        }
    } else {
        in.close();
        std::cerr << "unknown file format " << (int) header.datatypecode << "\n";
        return false;
    }
    if (!(header.imagedescriptor & 0x20)) {
        flip_vertically();
    }
    if (header.imagedescriptor & 0x10) {
        flip_horizontally();
    }
    std::cerr << width << "x" << height << "/" << bytespp * 8 << "\n";
    in.close();
    return true;
}

bool TGAImage::load_rle_data(std::ifstream &in) {
    auto pixelcount = static_cast<unsigned long>(width * height);
    unsigned long currentpixel = 0;
    unsigned long currentbyte = 0;
    TGAColor colorbuffer;
    do {
        unsigned char chunkheader = 0;
        chunkheader = static_cast<unsigned char>(in.get());
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

bool TGAImage::write(const char *filename, bool rle) {
    unsigned char developer_area_ref[4] = {0, 0, 0, 0};
    unsigned char extension_area_ref[4] = {0, 0, 0, 0};
    unsigned char footer[18] = {'T', 'R', 'U', 'E', 'V', 'I', 'S', 'I', 'O', 'N', '-', 'X', 'F', 'I', 'L', 'E', '.',
                                '\0'};
    std::ofstream out;
    out.open(filename, std::ios::binary);
    if (!out.is_open()) {
        std::cerr << "can't open file " << filename << "\n";
        out.close();
        return false;
    }
    TGA_Header header{};
    memset((void *) &header, 0, sizeof(header));
    header.bitsperpixel = static_cast<char>(bytespp << 3);
    header.width = static_cast<short>(width);
    header.height = static_cast<short>(height);
    header.datatypecode = static_cast<char>(bytespp == GRAYSCALE ? (rle ? 11 : 3) : (rle ? 10 : 2));
    header.imagedescriptor = 0x20;
    out.write((char *) &header, sizeof(header));
    if (!out.good()) {
        out.close();
        std::cerr << "can't dump the tga file\n";
        return false;
    }
    if (!rle) {
        out.write((char *) data, width * height * bytespp);
        if (!out.good()) {
            std::cerr << "can't unload raw data\n";
            out.close();
            return false;
        }
    } else {
        if (!unload_rle_data(out)) {
            out.close();
            std::cerr << "can't unload rle data\n";
            return false;
        }
    }
    out.write((char *) developer_area_ref, sizeof(developer_area_ref));
    if (!out.good()) {
        std::cerr << "can't dump the tga file\n";
        out.close();
        return false;
    }
    out.write((char *) extension_area_ref, sizeof(extension_area_ref));
    if (!out.good()) {
        std::cerr << "can't dump the tga file\n";
        out.close();
        return false;
    }
    out.write((char *) footer, sizeof(footer));
    if (!out.good()) {
        std::cerr << "can't dump the tga file\n";
        out.close();
        return false;
    }
    out.close();
    return true;
}

// TODO: it is not necessary to break a raw chunk for two equal pixels (for the matter of the resulting size)
bool TGAImage::unload_rle_data(std::ofstream &out) {
    const unsigned char max_chunk_length = 128;
    auto npixels = static_cast<unsigned long>(width * height);
    unsigned long curpix = 0;
    while (curpix < npixels) {
        unsigned long chunkstart = curpix * bytespp;
        unsigned long curbyte = curpix * bytespp;
        unsigned char run_length = 1;
        bool raw = true;
        while (curpix + run_length < npixels && run_length < max_chunk_length) {
            bool succ_eq = true;
            for (int t = 0; succ_eq && t < bytespp; t++) {
                succ_eq = (data[curbyte + t] == data[curbyte + t + bytespp]);
            }
            curbyte += bytespp;
            if (1 == run_length) {
                raw = !succ_eq;
            }
            if (raw && succ_eq) {
                run_length--;
                break;
            }
            if (!raw && !succ_eq) {
                break;
            }
            run_length++;
        }
        curpix += run_length;
        out.put(static_cast<char>(raw ? run_length - 1 : run_length + 127));
        if (!out.good()) {
            std::cerr << "can't dump the tga file\n";
            return false;
        }
        out.write((char *) (data + chunkstart), (raw ? run_length * bytespp : bytespp));
        if (!out.good()) {
            std::cerr << "can't dump the tga file\n";
            return false;
        }
    }
    return true;
}
