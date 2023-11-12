package com.z.wav;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavFileSaver {

    public static void saveWavFile(byte[] audioData, String filePath) {
        // Set WAV file parameters
        int sampleRate = 44100; // Sample rate in Hz
        int bitsPerSample = 16; // Bits per sample
        int numChannels = 1;    // Number of audio channels (1 for mono, 2 for stereo)

        // Calculate other parameters
        int byteRate = sampleRate * numChannels * bitsPerSample / 8;
        int audioDataLength = audioData.length;
        int totalDataLength = audioDataLength + 36;

        try {
            // Create a File object to represent the WAV file
            File wavFile = new File(filePath);

            // Create FileOutputStream to write to the file
            FileOutputStream outputStream = new FileOutputStream(wavFile);

            // Write WAV header
            writeWavHeader(outputStream, audioDataLength, totalDataLength, sampleRate, numChannels, byteRate, bitsPerSample);

            // Write audio data
            outputStream.write(audioData);

            // Close the FileOutputStream
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeWavHeader(FileOutputStream outputStream, int audioDataLength, int totalDataLength,
                                       int sampleRate, int numChannels, int byteRate, int bitsPerSample) throws IOException {
        // Write the RIFF header
        outputStream.write("RIFF".getBytes());
        outputStream.write(intToBytes(totalDataLength), 0, 4); // Total file length - 8

        // Write the WAV format
        outputStream.write("WAVE".getBytes());

        // Write the fmt subchunk
        outputStream.write("fmt ".getBytes());
        outputStream.write(intToBytes(16), 0, 4); // Subchunk1 size
        outputStream.write(shortToBytes((short) 1), 0, 2); // Audio format (1 for PCM)
        outputStream.write(shortToBytes((short) numChannels), 0, 2); // Number of channels
        outputStream.write(intToBytes(sampleRate), 0, 4); // Sample rate
        outputStream.write(intToBytes(byteRate), 0, 4); // Byte rate
        outputStream.write(shortToBytes((short) (numChannels * bitsPerSample / 8)), 0, 2); // Block align
        outputStream.write(shortToBytes((short) bitsPerSample), 0, 2); // Bits per sample

        // Write the data subchunk
        outputStream.write("data".getBytes());
        outputStream.write(intToBytes(audioDataLength), 0, 4); // Subchunk2 size
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value),
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24)
        };
    }

    private static byte[] shortToBytes(short value) {
        return new byte[]{
                (byte) (value),
                (byte) (value >> 8)
        };
    }
}
