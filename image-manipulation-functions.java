import java.awt.image.BufferedImage;
import java.awt.*;

public class Processor  
{
    /**
     * Reflects the image in the x axis
     * @param image Input image
     * @return Reflected image
     */
    public static BufferedImage reflectX(BufferedImage image) {
        BufferedImage reflected = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0, xr = image.getWidth() - 1; x < image.getWidth(); x++, xr--) {
                reflected.setRGB(xr, y, image.getRGB(x, y));
            }
        }

        return reflected;
    }

    /**
     * Reflects the image in the y acis
     * @param image Input image
     * @return Reflected image
     */
    public static BufferedImage reflectY(BufferedImage image) {
        BufferedImage reflected = createBlankClone(image);

        for(int y = 0, yr = image.getHeight() - 1; y < image.getHeight(); y++, yr--) {
            for(int x = 0; x < image.getWidth(); x++) {
                reflected.setRGB(x, yr, image.getRGB(x, y));
            }
        }

        return reflected;
    }

    /**
     * Rotates image counter clockwise
     * @param image Input image
     * @return Rotated image
     */
    public static BufferedImage rotateCCW(BufferedImage image) {
        BufferedImage rotated = new BufferedImage(
                image.getHeight(),
                image.getWidth(),
                BufferedImage.TYPE_INT_ARGB);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                rotated.setRGB(y, x, image.getRGB(x, y));
            }
        }

        // Algorithm assumes its in a cartesian coordinate system, which an image file is not (y increases down instead of up), so it needs to be reflected
        return reflectY(rotated);
    }

    /**
     * Rotates image clockwise
     * @param image Input image
     * @return Rotated image
     */
    public static BufferedImage rotateCW(BufferedImage image) {
        BufferedImage rotated = new BufferedImage(
                image.getHeight(),
                image.getWidth(),
                BufferedImage.TYPE_INT_ARGB);

        for(int y = 0, xx = image.getHeight() - 1; y < image.getHeight(); y++, xx--) {
            for(int x = 0, yy = image.getWidth() - 1; x < image.getWidth(); x++, yy--) {
                rotated.setRGB(xx, yy, image.getRGB(x, y));
            }
        }

        // Algorithm assumes its in a cartesian coordinate system, which an image file is not (y increases down instead of up), so it needs to be reflected
        return reflectY(rotated);
    }
    
    /**
     * Rotates image at any angle
     * @param image Input image
     * @param degrees Angle to rotate by
     * @return Rotated image
     */
    public static BufferedImage rotateAny(BufferedImage image, int degrees) {
        // Convert to radians
        float angle = degrees * ((float) Math.PI / 180f);

        // Get centre coordinates
        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        // Get the coordinates of the rotated points in the four corners to find new image size
        vec2 s1 = vec2.rotate(centreX, centreY, 0, 0, angle),
             s2 = vec2.rotate(centreX, centreY, image.getWidth(), 0, angle),
             s3 = vec2.rotate(centreX, centreY, 0, image.getHeight(), angle),
             s4 = vec2.rotate(centreX, centreY, image.getWidth(), image.getHeight(), angle);

        // Get max and min, find difference for the new image width and height,
        // Then calculate the translation to be applied to the sample points
        float maxX = max4(s1.x, s2.x, s3.x, s4.x),
              maxY = max4(s1.y, s2.y, s3.y, s4.y),
              minX = min4(s1.x, s2.x, s3.x, s4.x),
              minY = min4(s1.y, s2.y, s3.y, s4.y),
              width = maxX - minX,
              height = maxY - minY,
              tlX = (width - image.getWidth()) / 2f,
              tlY = (height - image.getHeight()) / 2f;
              
        BufferedImage rotated = new BufferedImage(
                (int) width,
                (int) height,
                BufferedImage.TYPE_INT_ARGB);

        // Apply translation in loop
        for(int y = -(int) tlY; y < rotated.getHeight()  -(int) tlY; y++) {
            for(int x =  -(int) tlX; x < rotated.getWidth()  -(int) tlX; x++) {
                vec2 rot = vec2.rotate(centreX, centreY, x, y, angle);

                // If rotated image out of original image bounds, do not try to assign a colour
                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                // Undo translation when setting colour
                int newX = x + (int) tlX;
                int newY = y + (int) tlY;

                rotated.setRGB(newX, newY, cur.getRGB());
            }
        }

        return rotated;
    }
    
    
    /**
     * Rotates image at any angle but image is not resized
     * @param image Input image
     * @param degrees Angle to rotate by
     * @return Rotated image
     */
    public static BufferedImage rotateAnyNoResize(BufferedImage image, int degrees) {
        BufferedImage rotated = createBlankClone(image);
        
        float angle = degrees * ((float) Math.PI / 180f);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;
        
        
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                vec2 rot = vec2.rotate(centreX, centreY, x, y, angle);

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                rotated.setRGB(x, y, cur.getRGB());
            }
        }

        return rotated;
    }

    /**
     * Makes the image greyscale
     * @param image Input image
     * @return Greyscale image
     */
    public static BufferedImage greyScale(BufferedImage image) {
        BufferedImage greyscale = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                // Get max rgb value and set max for every rgb values
                int colour = Math.max(current.getGreen(), current.getBlue());

                colour = Math.max(current.getRed(), colour);

                greyscale.setRGB(x, y, new Color(colour, colour, colour).getRGB());
            }
        }

        return greyscale;
    }

    /**
     * Inverts the colours of the image
     * @param image Input image
     * @return Inverted image
     */
    public static BufferedImage negative(BufferedImage image) {
        BufferedImage inverted = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));
                inverted.setRGB(x, y, new Color(
                        255 - current.getRed(),
                        255 - current.getGreen(),
                        255 - current.getBlue()
                ).getRGB());
            }
        }

        return inverted;
    }

    /**
     * Adds random waves of colours to the image
     * @param image Input image
     * @param intensity Alpha value of colours
     * @param seed Seed to add randomness
     * @return New image with colours added
     */
    public static BufferedImage rainbowWave(BufferedImage image, int intensity, float seed) {
        BufferedImage wave = createBlankClone(image);

        seed = (float) Math.cos(seed) * 20f; // Limits the magnitude of the seed to within 20 while keeping is pseudorandom because the algorithm breaks when the seed is too large

        float period = 100f * ((float) Math.abs(Math.sin(seed)) + .5f);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));
                // Use sine and cosine values on the x and y coordinates to generate an rgb value for each rgb values
                int r = alphaComposite(
                            alphaComposite(
                                Math.max(0, (int) (Math.sin((float) x / period + seed) * 255f)),
                                Math.max(0, (int) (Math.cos((float) y / period + seed * 23) * 255f)),
                            128
                            ),
                        current.getRed(), intensity),

                    g = alphaComposite(
                            alphaComposite(
                                Math.max(0, (int) (Math.cos((float) x / period + seed * 32) * 255f)),
                                Math.max(0, (int) (Math.cos((float) y / period - Math.PI) * 255f)),
                            128
                            ),
                        current.getGreen(), intensity),

                    b = alphaComposite(
                            alphaComposite(
                            Math.max(0, (int) (Math.sin((float) x / period - Math.PI - seed * 12) * 255f)),
                            Math.max(0, (int) (Math.sin((float) y / period + seed * 65) * 255f)),
                            128
                            ), current.getBlue(), intensity);

                wave.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        return wave;
    }

    /**
     * Adds a colourful gradient to the image
     * @param image Input image
     * @param amount Alpha of the colourful gradient
     * @return New image with colour added
     */
    public static BufferedImage rainbowGradient(BufferedImage image, int amount, float seed) {
        BufferedImage rainbow = createBlankClone(image);

        int seedx = (int) ((float) Math.cos(seed) * 20f),
            seedy = (int) ((float) Math.sin(seed) * 20f);

        // Set rgb x and y coordinates
        int hw = image.getWidth() / 2,
            hh = image.getHeight() / 2,
            rx = hw + seedx, ry = 0 + seedy,
            gx = 0 - seedx, gy = hh + hh / 2 + seedy,
            bx = image.getWidth() - seedx, by = gy + seedy;

        float max = distance(hw, 0, hh, 0);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {

                // Get the distance from x and y coordinate to the corresponding rgb value
                // coordinates, then somewhat normalise it
                rainbow.setRGB(x, y, new Color(
                   clampRGB(distance(x, rx, y, ry) / max),
                   clampRGB(distance(x, gx, y, gy) / max),
                   clampRGB(distance(x, bx, y, by) / max)
                ).getRGB());
            }
        }

        return mixAlpha(rainbow, image, amount);
    }

    /**
     * Adds red to the image
     * @param image Input image
     * @param amount Amount of red
     * @return Image with red added
     */
    public static BufferedImage red(BufferedImage image, int amount) {
        BufferedImage red = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                red.setRGB(x, y, new Color(clampRGB(current.getRed() + 1), 0, 0).getRGB());
            }
        }
        // Mix red image with alpha input
        return mixAlpha(red, image, amount);
    }

    /**
     * Adds green to the image
     * @param image Input image
     * @param amount Amount of green
     * @return Image with green added
     */
    public static BufferedImage green(BufferedImage image, int amount) {
        BufferedImage green = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                green.setRGB(x, y, new Color(0, clampRGB(current.getGreen() + 1), 0).getRGB());
            }
        }
        // Mix green image with alpha input
        return mixAlpha(green, image, amount);
    }

    /**
     * Adds blue to the image
     * @param image Input image
     * @param amount Amount of blue
     * @return Image with blue added
     */
    public static BufferedImage blue(BufferedImage image, int amount) {
        BufferedImage blue = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                blue.setRGB(x, y, new Color(0, 0, clampRGB(current.getBlue() + 1)).getRGB());
            }
        }
        // Mix blue image with alpha input
        return mixAlpha(blue, image, amount);
    }

    /**
     * Makes the image warmer
     * @param image Input image
     * @param amount Amount of warm to add
     * @return Warm image
     */
    public static BufferedImage warm(BufferedImage image, int amount) {
        BufferedImage warm = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                warm.setRGB(x, y, new Color(255, current.getGreen(), 0).getRGB());
            }
        }
        // Mix warm image with alpha input
        return mixAlpha(warm, image, amount);
    }

    /**
     * Makes image cooler
     * @param image Input image
     * @param amount Amount of cool to add
     * @return Cool image
     */
    public static BufferedImage cool(BufferedImage image, int amount) {
        BufferedImage blue = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                blue.setRGB(x, y, new Color(0, current.getGreen(), 255).getRGB());
            }
        }
        // Mix cooler image with alpha input
        return mixAlpha(blue, image, amount);
    }

    /**
     * Brightens the image
     * @param image Input image
     * @param amount Amount to brighten by
     * @return Brightened image
     */
    public static BufferedImage brighten(BufferedImage image, int amount) {
        BufferedImage brighten = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                int r = current.getRed(),
                    g = current.getGreen(),
                    b = current.getBlue();
                // Increase rgb values by amount
                brighten.setRGB(x, y, new Color(
                        clampRGB(r + amount),
                        clampRGB(g + amount),
                        clampRGB(b + amount)
                ).getRGB());
            }
        }

        return brighten;
    }

    /**
     * Darkens the image
     * @param image Input image
     * @param amount Amount to darken by
     * @return Darkened image
     */
    public static BufferedImage darken(BufferedImage image, int amount) {
        BufferedImage darken = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                int r = current.getRed(),
                    g = current.getGreen(),
                    b = current.getBlue();
                // Decrease rgb values by amount
                darken.setRGB(x, y, new Color(
                    clampRGB(r - amount),
                    clampRGB(g - amount),
                    clampRGB(b - amount)
                ).getRGB());
            }
        }

        return darken;
    }

    /**
     * Adds sepia to the image
     * Based off of https://www.geeksforgeeks.org/image-procesing-java-set-6-colored-image-sepia-image-conversion/
     * @param image Input image
     * @param amount Amount of sepia to add
     * @return Image with sepia added
     */
    public static BufferedImage sepia(BufferedImage image,  int amount) {
        BufferedImage sepia = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));
                float r = current.getRed() / 255f;
                float g = current.getGreen() / 255f;
                float b = current.getGreen() / 255f;
                // Apply sepia algorithm
                sepia.setRGB(x, y, new Color(
                        (int)(clampRGB((float) (0.393*r + 0.769*g + 0.189*b)) * 255),
                        (int)(clampRGB((float) (0.349*r + 0.686*g + 0.168*b)) * 255),
                        (int)(clampRGB((float) (0.272*r + 0.534*g + 0.131*b)) * 255)
                ).getRGB());
            }
        }

        return mixAlpha(sepia, image, amount);
    }

    /**
     * Saturates the image
     * @param image Input image
     * @param amount Amount to saturate by
     * @return Saturated image
     */
    public static BufferedImage saturate(BufferedImage image, int amount) {
        BufferedImage saturate = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                Color current = new Color(image.getRGB(x, y));

                // Create a fully saturated image
                saturate.setRGB(x, y, new Color(
                        current.getRed() < 128 ? 0 : 255,
                        current.getGreen() < 128 ? 0 : 255,
                        current.getBlue() < 128 ? 0 : 255
                ).getRGB());
            }
        }
        // Mix fully saturated with original
        return mixAlpha(saturate, image, amount);
    }

    /**
     * Adds blur to the image
     * @param image Input image
     * @param range Range of pixels in a square area to sample
     * @return Blurred image
     */
    public static BufferedImage blur(BufferedImage image, int range) {
        BufferedImage blur = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                int passes = 0;
                int r = 0, g = 0, b = 0;

                // Get average colour from neighbouring pixels
                for(int xx = Utils.clamp(x - range, 0, image.getWidth()); xx < Utils.clamp(x + range + 1, 0, image.getWidth()); xx++) {
                    for(int yy = Utils.clamp(y - range, 0, image.getHeight()); yy < Utils.clamp(y + range + 1, 0, image.getHeight()); yy++) {
                        passes++;
                        Color c = new Color(image.getRGB(xx, yy));
                        r += c.getRed();
                        g += c.getGreen();
                        b += c.getBlue();
                    }
                }

                // Set average colour
                blur.setRGB(x, y, new Color(
                    r / passes,
                    g / passes,
                    b / passes
                ).getRGB());
            }
        }

        return blur;
    }

    /**
     * Blurs image with multiple passes through the blur function
     * @param image Input image
     * @param range Range of pixels in a square area to sample
     * @param passes Number of passes
     * @return Blurred image
     */
    public static BufferedImage blurPasses(BufferedImage image, int range, int passes) {
        BufferedImage last;

        last = blur(image, range);

        for(int i = 0; i < passes - 1; i++) {
            last = blur(last, range);
        }

        return last;
    }

    /**
     * Distorts image
     * @param image Input image
     * @return Distorted image
     */
    public static BufferedImage distortWave(BufferedImage image) {
        BufferedImage distort = createBlankClone(image);
        
        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Use sine and cosine functions to change the sample coordinates
                Color c = new Color(image.getRGB(
                        Utils.clamp((int) (x + (Math.sin((float) x / 18f)) * 10f), 0, image.getWidth() - 1),
                        Utils.clamp((int) (y + (Math.cos((float) y / 20f)) * 10f ), 0, image.getHeight() - 1)
                ));

                distort.setRGB(x, y, c.getRGB());
            }
        }

        return distort;
    }

    /**
     * Distorts the image vertically
     * @param image Input image
     * @return Distorted image
     */
    public static BufferedImage distortWaveX(BufferedImage image) {
        BufferedImage distort = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Use sine function to change the sample coordinates
                Color c = new Color(image.getRGB(
                        Utils.clamp((int) (x + (Math.sin((float) x / 18f)) * 10f), 0, image.getWidth() - 1),
                        y
                ));

                distort.setRGB(x, y, c.getRGB());
            }
        }

        return distort;
    }

    /**
     * Distorts the image horizontally
     * @param image Input image
     * @return Distorted image
     */
    public static BufferedImage distortWaveY(BufferedImage image) {
        BufferedImage distort = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Use cosine function to change the sample coordinates
                Color c = new Color(image.getRGB(
                        x,
                        Utils.clamp((int) (y + (Math.cos((float) y / 20f)) * 10f), 0, image.getHeight() - 1)
                ));


                distort.setRGB(x, y, c.getRGB());
            }
        }

        return distort;
    }
    
    /**
     * Adds a shimmering effect
     * 
     * @param image Input image
     * @return Image with shimmer added
     */
    public static BufferedImage shimmer(BufferedImage image, int amount) {
        BufferedImage shimmer = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Sample from a random pixel near the current pixel
                Color c = new Color(image.getRGB(
                     Utils.clamp((int) (x + (float) (Math.random() -.5)* 5 * 10f), 0, image.getWidth() - 1),
                     Utils.clamp((int) (y + (float) (Math.random() -.5)* 5 * 10f), 0, image.getHeight() - 1)
                ));

                shimmer.setRGB(x, y, c.getRGB());
            }
        }

        return mixAlpha(shimmer, image, amount);
    }

    /**
     * Extracts the image as a watermark assuming it has a white background
     * Code from an earlier project: https://github.com/hpnrep6/ImageWatermarkRemover
     *
     * @param image Input image
     * @return Extracted watermark
     */
    public static BufferedImage extractWatermarkWhiteBackground(BufferedImage image) {
        // BufferedImage object for transparent watermark on white background
        BufferedImage watermarkOnlyImage = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int r, g, b, a, max; // red, green, blue, alpha/opacity/transparency
                Color curColour = new Color(image.getRGB(x, y));

                // Alpha value = 0 if all equals 0
                if(Math.max(curColour.getRed(),Math.max(curColour.getBlue(),curColour.getGreen())) == 255) {
                    a = 0; r = 0; g = 0; b = 0;
                }
                else {
                    // Alpha values equals max of values after they are subtracted from 255
                    a = Math.max(255 - curColour.getRed(),Math.max(255 - curColour.getBlue(),255 - curColour.getGreen()));
                    // Formula is C = Cs * a + Cd ( 1 - a )
                    // C = Final Colour | Cs = Colour with alpha added on top | Cd = Colour underneath / original colour | a = Alpha / opacity / transparency value from 0.0 to 1.0
                    r = (int)(((255 - (255.0*(255 - curColour.getRed())) / a)));
                    g = (int)(((255 - (255.0*(255 - curColour.getGreen())) / a)));
                    b = (int)(((255 - (255.0*(255 - curColour.getBlue())) / a)));
                }
                // creates a colour object using the RGB values and adds the integer ARGB value into the BufferedImage object
                watermarkOnlyImage.setRGB(x,y,new Color(r,g,b,a).getRGB());
            }
        }
        return watermarkOnlyImage;
    }

    /**
     * Removes a transparent watermark on an image
     * Code from an earlier project: https://github.com/hpnrep6/ImageWatermarkRemover
     *
     * @param watermarked Watermarked image
     * @param watermark Transparent watermark
     * @param xCoord Top left coordinate of the watermark in the x-axis
     * @param yCoord Top left coordinate of the watermark in the y-axis
     * @return Unwatermarked image
     */
    public static BufferedImage removeWatermark(BufferedImage watermarked, BufferedImage watermark, int xCoord, int yCoord) {
        BufferedImage image = Utils.deepCopy(watermarked);

        for(int x = 0, x2 = xCoord; x < watermark.getWidth() && x2 < watermarked.getWidth(); x++, x2++) {
            for (int y = 0, y2 = yCoord; y < watermark.getHeight() && y2 < watermarked.getHeight(); y++, y2++) {
                if(x2 < 0 || y2 < 0) continue;
                
                int r, g, b, or, og, ob, wr, wg, wb; // red, green, blue values for new, original and watermark
                double a; // alpha/transparency/opacity value
                Color curColour = new Color(image.getRGB(x2, y2));
                Color watermarkColour = new Color(watermark.getRGB(x,y), true);
                // original / watermarked image RGB values
                or = curColour.getRed();
                og = curColour.getGreen();
                ob = curColour.getBlue();
                // separated watermark image RGB values
                wr = watermarkColour.getRed();
                wg = watermarkColour.getGreen();
                wb = watermarkColour.getBlue() ;
                // alpha value of watermark
                a = (watermarkColour.getAlpha()) / 255.0;
                // final RGB values after watermark has been removed
                // reverses the formula for alpha compositing to solve for the initial background colour instead of the final colour
                // C = Cs * a + Cd ( 1 - a ) -> Cd = (C - Cs * a) / (1 - a)
                r = (int) ((or - wr * a) / (1 - a));
                g = (int) ((og - wg * a) / (1 - a));
                b = (int) ((ob - wb * a) / (1 - a));
                // limits RGB values to be within 0 to 255 to ensure colour is set every time because the RGB calculations above will
                // return negative integers or integers greater than 255
                if(r >= 255) { r = 255; }
                if(g >= 255) { g = 255; }
                if(b >= 255) { b = 255; }
                if(r < 0) { r = 0; }
                if(g < 0) { g = 0; }
                if(b < 0) { b = 0; }
                // set RGB colour
                image.setRGB(x2, y2, new Color(r, g, b).getRGB());
            }
        }
        return image;
    }

    /**
     * Adds a transparent watermark onto another image
     * Code from a previous project: https://github.com/hpnrep6/ImageWatermarkRemover
     *
     * @param unwatermarked
     * @param watermark
     * @param xCoord
     * @param yCoord
     * @return
     */
    public static BufferedImage addWatermark(BufferedImage unwatermarked, BufferedImage watermark, int xCoord, int yCoord) {
        BufferedImage image = Utils.deepCopy(unwatermarked);

        for(int x = 0, x2 = xCoord; x <  watermark.getWidth() && x2 < unwatermarked.getWidth(); x++, x2++) {
            for (int y = 0, y2 = yCoord; y < watermark.getHeight() && y2 < unwatermarked.getHeight(); y++, y2++) {
                if(x2 < 0 || y2 < 0) continue;
            
                int r, g, b, or, og, ob, wr, wg, wb; // red, green, blue values for new, original and watermark
                double a; // alpha/transparency/opacity value
                Color curColour = new Color(image.getRGB(x2, y2));
                Color watermarkColour = new Color(watermark.getRGB(x,y), true);
                // original / watermarked image RGB values
                or = curColour.getRed();
                og = curColour.getGreen();
                ob = curColour.getBlue();
                // separated watermark image RGB values
                wr = watermarkColour.getRed();
                wg = watermarkColour.getGreen();
                wb = watermarkColour.getBlue() ;
                // alpha value of watermark
                a = (watermarkColour.getAlpha()) / 255.0;
                // Adds watermark onto image using the alpha compositing formula
                r = (int) ((wr * a) + or * (1 - a));
                g = (int) ((wg * a) + og * (1 - a));
                b = (int) ((wb * a) + ob * (1 - a));
                // limits RGB values to be within 0 to 255 to ensure colour is set every time because the RGB calculations above will
                // return negative integers or integers greater than 255
                if(r >= 255) { r = 255; }
                if(g >= 255) { g = 255; }
                if(b >= 255) { b = 255; }
                if(r < 0) { r = 0; }
                if(g < 0) { g = 0; }
                if(b < 0) { b = 0; }
                // set RGB colour
                image.setRGB(x2, y2, new Color(r, g, b).getRGB());
            }
        }
        return image;
    }

    /**
     * Adds random noise onto the image
     * @param image Input image
     * @param amount Alpha of noise
     * @return Image with noise added
     */
    public static BufferedImage noise(BufferedImage image, int amount) {
        BufferedImage noise = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                noise.setRGB(x, y,
                new Color(
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random()
                ).getRGB());
            }
        }
        // Mix noise with original
        return mixAlpha(noise, image, amount);
    }

    /**
     * Adds random greyscaled noise onto the image
     * @param image Input image
     * @param amount Alpha of noise
     * @return Image with noise added
     */
    public static BufferedImage noiseGreyscale(BufferedImage image, int amount) {
        BufferedImage noise = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {

                float rand = (float) Math.random();

                noise.setRGB(x, y, new Color(rand, rand, rand).getRGB());
            }
        }
        // Mix noise with original
        return mixAlpha(noise, image, amount);
    }

    /**
     * Pixelates the image
     * @param image Input image
     * @param pixelSize Size of pixels
     * @return Pixelated image
     */
    public static BufferedImage pixel(BufferedImage image, int pixelSize) {
        BufferedImage pixel = Utils.deepCopy(image);

        for(int y = 0; y < image.getHeight(); y += pixelSize) {
            for(int x = 0; x < image.getWidth(); x += pixelSize) {
                // Set neightbouring pixels to same colour as current, then jump pixelSize steps to
                // the next set of pixels
                for(int xx = x; xx < x + pixelSize && xx < image.getWidth(); xx++) {
                    for (int yy = y; yy < y + pixelSize && yy < image.getHeight(); yy++) {
                        pixel.setRGB(xx, yy, new Color(image.getRGB(x, y)).getRGB());
                    }
                }
                
            }
        }

        return pixel;
    }

    /**
     * Creates a circular fade effect
     * @param image Input image
     * @param colour Colour to fade into
     * @param radius Radius of circle
     * @param fadeLength Length of fade
     * @return Faded image
     */
    public static BufferedImage circleFade(BufferedImage image, Color colour, int radius, int fadeLength) {
        BufferedImage fade = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Set colour relative to coordinate distance from centre of image
                float dist = distance(x, centreX, y, centreY);

                float alpha = (dist / (radius + fadeLength)) * 255f;

                Color current = new Color(image.getRGB(x, y));

                fade.setRGB(x, y, new Color(
                        alphaComposite(colour.getRed(), current.getRed(), (int) alpha),
                        alphaComposite(colour.getGreen(), current.getGreen(), (int) alpha),
                        alphaComposite(colour.getBlue(), current.getBlue(), (int) alpha)
                ).getRGB());
            }
        }

        return fade;
    }

    /**
     * Creates a square fade effect
     * @param image Input image
     * @param colour Colour to fade into
     * @param width Width of square
     * @param fadeLength Length of fade
     * @return Faded image
     */
    public static BufferedImage squareFade(BufferedImage image, Color colour, int width, int fadeLength) {
        BufferedImage fade = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Set colour relative to the maximum distance from a line down the vertical and a line down the horizontal
                float distX = distance1d(x, centreX),
                      distY = distance1d(y, centreY),
                      dist = Math.max(distX, distY);

                float alpha = (dist / (width + fadeLength)) * 255f;

                Color current = new Color(image.getRGB(x, y));

                fade.setRGB(x, y, new Color(
                        alphaComposite(colour.getRed(), current.getRed(), (int) alpha),
                        alphaComposite(colour.getGreen(), current.getGreen(), (int) alpha),
                        alphaComposite(colour.getBlue(), current.getBlue(), (int) alpha)
                ).getRGB());
            }
        }

        return fade;
    }
    
    /**
     * Creates a circular shape made out of randomised points sampled from the image
     * @param image Input image
     * @param amount Alpha of disfigured image
     * @return Disfigured image
     */
    public static BufferedImage circleDisfigure(BufferedImage image, int amount) {
        BufferedImage disfigured = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Apply a random rotation to the point being sampled
                vec2 rot = vec2.rotate(centreX, centreY, x, y, (float) Math.cos(x * y));

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;
                
                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                disfigured.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(disfigured, image, amount);
    }

    /**
     * Distorts the image in a circular pattern
     * @param image Input image
     * @return Distorted image
     */
    public static BufferedImage circleDistort(BufferedImage image, int amount) {
        BufferedImage distort = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Rotate the point being sampled relative to the distance from the centre
                float dist = distance(centreX, x, centreY, y);

                vec2 rot = vec2.rotate(centreX, centreY, x, y, dist / (Math.min(image.getWidth(), image.getHeight() )/ 10f));

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                distort.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(distort, image, amount);
    }

    /**
     * Distorts image in the pattern of a concave shape
     * @param image Input image
     * @param alpha Alpha of distorted image
     * @return Distorted image
     */
    public static BufferedImage concaveDistort(BufferedImage image, int alpha) {
        BufferedImage distort = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        float div = Math.max(image.getWidth(), image.getHeight());

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale the image from the centre relative to the angle to the centre
                float dist = (float) Math.cos(angleTo(centreX, x, centreY, y) * 4f);
                vec2 scale = vec2.scale(centreX, centreY, x, y, dist);

                if(scale.x < 0 || scale.x >= image.getWidth() || scale.y < 0 || scale.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) scale.x, (int) scale.y));

                distort.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(distort, image, alpha);
    }

    /**
     * Distorts image as if it was a reflection on a sphere
     * @param image Input image
     * @param alpha Alpha of distorted image
     * @return Distorted image
     */
    public static BufferedImage sphereDistort(BufferedImage image, int alpha) {
        BufferedImage distort = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        float div = Math.max(image.getWidth(), image.getHeight());

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale the image relative to the distance from the centre, with the distance being put through a
                // sine function and thus limiting the scale from -1 to 1
                int dist = (int) distance(centreX, x, centreY, y);
                vec2 scale = vec2.scale(centreX, centreY, x, y, (float) Math.sin(dist / 50f));

                if(scale.x < 0 || scale.x >= image.getWidth() || scale.y < 0 || scale.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) scale.x, (int) scale.y));

                distort.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(distort, image, alpha);
    }

    /**
     * Creates a circular ripple effect on the image
     * @param image Input image
     * @param alpha Alpha of ripple
     * @return Rippled image
     */
    public static BufferedImage ripple(BufferedImage image, int alpha) {
        BufferedImage ripple = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale the image relative to the distance from the centre, with the distance being put through a
                // sine function and thus limiting the scale from -1 to 1
                // (sphereDistort but sine wave has a shorter period)
                int dist = (int) distance(centreX, x, centreY, y);
                vec2 scale = vec2.scale(centreX, centreY, x, y, (float) Math.sin(dist / 6f));

                if(scale.x < 0 || scale.x >= image.getWidth() || scale.y < 0 || scale.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) scale.x, (int) scale.y));

                ripple.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(ripple, image, alpha);
    }

    /**
     * Distorts the image by stretching it along the sides
     * @param image Input image
     * @return Distorted image
     */
    public static BufferedImage scaleOut(BufferedImage image) {
        BufferedImage zoom = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        float div = Math.max(image.getWidth(), image.getHeight());

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale relative to distance from centre

                int dist = (int) distance(centreX, x, centreY, y);

                vec2 rot = vec2.scale(centreX, centreY, x, y, (div - (float) dist) / div);

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                zoom.setRGB(x, y, cur.getRGB());
            }
        }

        return zoom;
    }

    /**
     * Distorts image in a fisheye-like way
     * @param image Input image
     * @param alpha Alpha of distorted image
     * @return Distorted image
     */
    public static BufferedImage bulge(BufferedImage image, int alpha) {
        BufferedImage bulge = createBlankClone(image);

        int centreX = image.getWidth() / 2;
        int centreY = image.getHeight() / 2;

        float div = Math.min(image.getWidth(), image.getHeight());

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale relative to distance from centre
                int dist = (int) distance(centreX, x, centreY, y);

                vec2 rot = vec2.scale(centreX, centreY, x, y, (float) dist / div);

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                bulge.setRGB(x, y, cur.getRGB());
            }
        }

        return mixAlpha(bulge, image, alpha);
    }

    /**
     * Curves the image up, like the edge of a piece of paper
     * @param image Input image
     * @return Curved image
     */
    public static BufferedImage curveUp(BufferedImage image) {
        BufferedImage curve = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale relative to the y axis
                vec2 rot = vec2.scale(0, 0, x, y, (float) y / ((float) image.getHeight()));

                if(rot.x < 0 || rot.x >= image.getWidth() || rot.y < 0 || rot.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) rot.x, (int) rot.y));

                curve.setRGB(x, y, cur.getRGB());
            }
        }

        return curve;
    }

    /**
     * Curves the image right, like the edge of a piece of paper
     * @param image Input image
     * @return Curved image
     */
    public static BufferedImage curveRight(BufferedImage image) {
        BufferedImage curve = createBlankClone(image);

        for(int y = 0; y < image.getHeight(); y++) {
            for(int x = 0; x < image.getWidth(); x++) {
                // Scale relative to the x axis
                vec2 scale = vec2.scale(0, 0, x, y, (float) x / ((float) image.getWidth()));

                if(scale.x < 0 || scale.x >= image.getWidth() || scale.y < 0 || scale.y >= image.getHeight()) continue;

                Color cur = new Color(image.getRGB((int) scale.x, (int) scale.y));

                curve.setRGB(x, y, cur.getRGB());
            }
        }

        return curve;
    }

    // Create an empty BufferedImage of the input's width and height
    private static BufferedImage createBlankClone(BufferedImage img) {
        return new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
    }

    // Get the final colour from a solid background colour and a colour with alpha
    private static int alphaComposite(int colour, int base, int alpha) {
        float cf2 = (float) base / 255f;
        float cf1 = (float) colour / 255f;
        float af1 = (float) alpha / 255f;

        int result = (int) ((cf1 * af1 + (1f - af1) * cf2) * 255f);

        // Clamp within 0 and 255
        return Math.min(255, Math.max(0, result));
    }

    // Add one image on top of another, with the image on top having the specified alpha value
    private static BufferedImage mixAlpha(BufferedImage a, BufferedImage b, int alpha) {
        if(a.getWidth() != b.getWidth()) throw new Error("Invalid parameter for function mixAlpha: Widths do not match for both BufferedImages");

        if(a.getHeight() != b.getHeight()) throw new Error("Invalid parameter for function mixAlpha: Heights do not match for both BufferedImages");

        BufferedImage mixed = createBlankClone(a);

        for(int y = 0; y < a.getHeight(); y++) {
            for(int x = 0; x < a.getWidth(); x++) {
                Color aC = new Color(a.getRGB(x, y));
                Color bC = new Color(b.getRGB(x, y));

                mixed.setRGB(x, y, new Color(
                        alphaComposite(aC.getRed(), bC.getRed(), alpha),
                        alphaComposite(aC.getGreen(), bC.getGreen(), alpha),
                        alphaComposite(aC.getBlue(), bC.getBlue(), alpha)
                ).getRGB());
            }
        }

        return mixed;
    }

    // Gets max of 4 floats
    private static float max4(float a, float b, float c, float d) {
        return Math.max(a, Math.max(b, Math.max(c, d)));
    }

    // Gets min of 4 floats
    private static float min4(float a, float b, float c, float d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }
    
    // Clamps within 0 and 1 for RGBA values
    private static float clampRGB(float f) {
        return f > 1f ? 1f : f < 0 ? 0 : f;
    }

    // Clamps within 0 and 255 for RGBA values
    private static int clampRGB(int f) {
        return f > 255 ? 255 : f < 0 ? 0 : f;
    }

    // Distance between two points in two dimensions
    private static float distance(int x1, int x2, int y1, int y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // Distance between two points in one dimension
    private static int distance1d(int x, int x2) {
        return Math.abs(x - x2);
    }

    // Distance between two points in one dimension
    private static float distance1d(float x, float x2) {
        return Math.abs(x - x2);
    }
    
    /**
     * Calculates the distance of two locations.
     *
     * @param fromX       the x location to calculate from
     * @param toX         the x location to calculate the angle to
     * @param fromY       the y location to calculate from
     * @param toX         the y location to calculate the angle to
     * @return float      the angle in radians
     */
    private static float angleTo(int fromX, int toX, int fromY, int toY) {
        return (float) Math.atan2( (float) (toY - fromY), (float) (toX - fromX));
    }
    
    /**
     * Class for 2 dimensional vector and point maths
     */
    private static class vec2 {
        public float x, y;
            
        /**
         * Creates a vec2 from two values
         * @param x X-component
         * @param y Y-component
         */
        private vec2(float x, float y) {
            this.x = x;
            this.y = y;
        }
    
        /**
         * Creates a vec2 from another vec2
         * @param v Vec2 to clone from
         */
        private vec2(vec2 v) {
            this.x = v.x;
            this.y = v.y;
        }
    
        /**
         * Performs rotation around a point
         * @param ox X coordinate of point to rotate from
         * @param oy Y coordinate of point to rotate from
         * @param px X coordinate of point being rotated
         * @param py Y coordinate of point being rotate
         * @param radians Angle of rotation in radians
         */
        public static vec2 rotate(int ox, int oy, int px, int py, float radians) {
            // Get x and y components of angle
            float sin = (float) Math.sin(radians);
            float cos = (float) Math.cos(radians);
    
            // Translate to origin
            px -= ox;
            py -= oy;
    
            // Perform rotation, then translate back from origin
            return new vec2(
                px * cos - py * sin + ox,
                px * sin + py * cos + oy
            );
        }

        /**
         * Scales vector originating from a certain point in 2 dimensional space
         * @param ox X coordinate of point to scale from
         * @param oy Y coordinate of point to scale from
         * @param px X coordinate of point being scaled
         * @param py Y coordinate of point being scaled
         * @param scale Factor to scale by
         * @return Scaled vec2 at the correct coordinates relative to the cartesian plane
         */
        public static vec2 scale(int ox, int oy, int px, int py, float scale) {
            // Translate to origin
            px -= ox;
            py -= oy;
    
            // Scale and translate back from origin
            return new vec2(
                px * scale + ox,
                py * scale + oy
            );
        }
    }
}
