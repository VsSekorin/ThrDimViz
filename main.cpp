#include <iostream>
#include "tgaimage.h"

int main() {
    const auto size = 1000;
    TGAImage image(size, size, TGAImage::RGB);
//    image.draw();
    image.flip_vertically();
    image.write("out.tga");
    return 0;
}