package io.gameover.utilities.pixeleditor.utils;

import io.gameover.utilities.pixeleditor.Pixelizer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contract for implementation class which save a list of frame as a file.
 */
public interface Encoder {

    void saveImage(File f, List<Pixelizer.Frame> frames) throws IOException;
}
