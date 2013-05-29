package io.gameover.utilities.pixeleditor.utils;

import io.gameover.utilities.pixeleditor.Frame;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Contract for implementation class which save a list of frame as a file.
 */
public interface Encoder {

    void saveImage(File f, List<Frame> frames) throws IOException;
}
