/*
 *   MpegFormatConversionProvider.
 *
 * JavaZOOM : mp3spi@javazoom.net
 * 			  http://www.javazoom.net
 *
 * ---------------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --------------------------------------------------------------------------
 */
package com.github.tartaricacid.netmusic.client.audio

import javazoom.spi.mpeg.sampled.convert.DecodedMpegAudioInputStream
import javazoom.spi.mpeg.sampled.file.MpegEncoding
import org.tritonus.share.TDebug
import org.tritonus.share.sampled.Encodings
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class MpegFormatConversionProvider :
    TEncodingFormatConversionProvider(listOf(*INPUT_FORMATS), listOf(*OUTPUT_FORMATS)) {

    companion object {
        private val MPEG1L3: AudioFormat.Encoding = Encodings.getEncoding("MPEG1L3")

        private val PCM_SIGNED: AudioFormat.Encoding = Encodings.getEncoding("PCM_SIGNED")

        private val INPUT_FORMATS = arrayOf(
            // mono
            AudioFormat(MPEG1L3, -1.0f, -1, 1, -1, -1.0f, false),
            AudioFormat(MPEG1L3, -1.0f, -1, 1, -1, -1.0f, true),  // stereo
            AudioFormat(MPEG1L3, -1.0f, -1, 2, -1, -1.0f, false),
            AudioFormat(MPEG1L3, -1.0f, -1, 2, -1, -1.0f, true),
        )


        private val OUTPUT_FORMATS = arrayOf(
            // mono, 16-bit signed
            AudioFormat(PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, false),
            AudioFormat(PCM_SIGNED, -1.0f, 16, 1, 2, -1.0f, true),  // stereo, 16 bit signed
            AudioFormat(PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, false),
            AudioFormat(PCM_SIGNED, -1.0f, 16, 2, 4, -1.0f, true),
        )
    }

    init {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider()")
        }
    }

    override fun getAudioInputStream(targetFormat: AudioFormat, audioInputStream: AudioInputStream): AudioInputStream {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider.getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream):")
        }
        return DecodedMpegAudioInputStream(targetFormat, audioInputStream)
    }

    /**
     * Add conversion support for any MpegEncoding source with FrameRate or FrameSize not empty.
     *
     * @param targetFormat
     * @param sourceFormat
     * @return
     */
    override fun isConversionSupported(targetFormat: AudioFormat, sourceFormat: AudioFormat): Boolean {
        if (TDebug.TraceAudioConverter) {
            TDebug.out(">MpegFormatConversionProvider.isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat):")
            TDebug.out("checking if conversion possible")
            TDebug.out("from: $sourceFormat")
            TDebug.out("to: $targetFormat")
        }

        var conversion = super.isConversionSupported(targetFormat, sourceFormat)
        if (!conversion) {
            val enc = sourceFormat.encoding
            if (enc is MpegEncoding) {
                if ((sourceFormat.frameRate != AudioSystem.NOT_SPECIFIED.toFloat())
                    || (sourceFormat.frameSize != AudioSystem.NOT_SPECIFIED)) {
                    conversion = true
                }
            }
        }

        return conversion
    }
}