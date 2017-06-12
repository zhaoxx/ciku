package com.mia.ciku.download;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterOutputStream;

/**
 * QQ Pinyin IME QPYD File Reader
 * 
 * QPYD Format overview:
 * 
 * General Information:
 * - Chinese characters are all encoded with UTF-16LE.
 * - Pinyin are encoded in ascii (or UTF-8).
 * - Numbers are using little endian byte order.
 * 
 * QPYD hex analysis:
 * - 0x00 QPYD file identifier
 * - 0x38 offset of compressed data (word-pinyin-dictionary)
 * - 0x44 total words in qpyd
 * - 0x60 start of header information
 * 
 * Compressed data analysis:
 * - zip/standard (beginning with 0x789C) is used in (all analyzed) qpyd files
 * - data is divided in two parts
 * -- 1. offset and length information (16 bytes for each pinyin-word pair)
 *       0x06 offset points to first pinyin
 *       0x00 length of pinyin
 *       0x01 length of word
 * -- 2. actual data
 *       Dictionary data has the form ((pinyin)(word))* with no separators.
 *       Data can only be read using offset and length information.
 * 
 * 
 * @author keke
 */
public class QQPinyinQpydReader {
	public QQScelMdel read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public QQScelMdel read(URL url) throws IOException {
        return read(url.openStream());
    }

    protected ByteArrayOutputStream output=new ByteArrayOutputStream();

    protected String readString(DataInputStream input,int pos,int[] reads) throws IOException {
        int read=reads[0];
        input.skip(pos-read);
        read=pos;
        output.reset();
        while(true) {
            int c1 = input.read();
            int c2 = input.read();
            read+=2;
            if(c1==0 && c2==0) {
                break;
            } else {
                output.write(c1);
                output.write(c2);
            }
        }
        reads[0]=read;
        return new String(output.toByteArray(),encoding);
    }

    protected static String encoding = "UTF-16LE";

    public QQScelMdel read(InputStream in) throws IOException {
    	QQScelMdel model = new QQScelMdel();
        try {
        	byte[] bytes = toByteArray(in);
        	 ByteBuffer dataRawBytes = ByteBuffer.wrap(bytes);
        	 dataRawBytes.order(ByteOrder.LITTLE_ENDIAN);

             // read info of compressed data
             int startZippedDictAddr = dataRawBytes.getInt(0x38);
             int zippedDictLength = dataRawBytes.limit() - startZippedDictAddr;

             // qpys as UTF-16LE string
             String dataString = new String(Arrays.copyOfRange(dataRawBytes.array(), 0x60, startZippedDictAddr), encoding);
             // print header
             System.out.println("名称：" + substringBetween(dataString, "Name: ", "\r\n"));
             System.out.println("类型：" + substringBetween(dataString, "Type: ", "\r\n"));
             System.out.println("子类型：" + substringBetween(dataString, "FirstType: ", "\r\n"));
             System.out.println("词库说明：" + substringBetween(dataString, "Intro: ", "\r\n"));
             System.out.println("词库样例：" + substringBetween(dataString, "Example: ", "\r\n"));
             System.out.println("词条数：" + dataRawBytes.getInt(0x44));
             
          // read zipped qqyd dictionary into byte array
             ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
             dataOut.reset();
             Channels.newChannel(new InflaterOutputStream(dataOut)).write(
                     ByteBuffer.wrap(dataRawBytes.array(), startZippedDictAddr, zippedDictLength));

             // uncompressed qqyd dictionary as bytes
             ByteBuffer dataUnzippedBytes = ByteBuffer.wrap(dataOut.toByteArray());
             dataUnzippedBytes.order(ByteOrder.LITTLE_ENDIAN);

             // for debugging: save unzipped data to *.unzipped file
//             Channels.newChannel(new FileOutputStream(qqydFile + ".unzipped")).write(dataUnzippedBytes);
//             System.out.println("压缩数据：0xnteger.toHexString(startZippedDictAddr) + " (解压前：" + zippedDictLength
//                     + " B, 解压后：" + dataUnzippedBytes.limit() + " B)");
             
             // stores the start address of actual dictionary data
             int unzippedDictStartAddr = -1;
             int idx = 0;
             byte[] byteArray = dataUnzippedBytes.array();
             
             List<String> wordList= new ArrayList<String>();
             
             while (unzippedDictStartAddr == -1 || idx < unzippedDictStartAddr) {
                 // read word
                 int pinyinStartAddr = dataUnzippedBytes.getInt(idx + 0x6);
                 int pinyinLength = dataUnzippedBytes.get(idx + 0x0) & 0xff;
                 int wordStartAddr = pinyinStartAddr + pinyinLength;
                 int wordLength = dataUnzippedBytes.get(idx + 0x1) & 0xff;
                 if (unzippedDictStartAddr == -1) {
                     unzippedDictStartAddr = pinyinStartAddr;
//                     System.out.println("词库地址（解压后）：0xnteger.toHexString(unzippedDictStartAddr) + "\n");
                 }

//                 String pinyin = new String(Arrays.copyOfRange(byteArray, pinyinStartAddr, pinyinStartAddr + pinyinLength),
//                         "UTF-8");
                 String word = new String(Arrays.copyOfRange(byteArray, wordStartAddr, wordStartAddr + wordLength),
                         "UTF-16LE");
//                 System.out.println(word);
                 wordList.add(word);
                 // step up
                 idx += 0xa;
             }
             model.setWordList(wordList);
             return model;
        } finally {
            in.close();
        }
    }
    
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[1024*4];
        int n=0;
        while ( (n=in.read(buffer)) !=-1) {
            out.write(buffer,0,n);
        }
        return out.toByteArray();
    }
    
    public static final String substringBetween(String text, String start, String end) {
        int nStart = text.indexOf(start);
        int nEnd = text.indexOf(end, nStart + 1);
        if (nStart != -1 && nEnd != -1) {
            return text.substring(nStart + start.length(), nEnd);
        } else {
            return null;
        }
    }

    protected final int readUnsignedShort(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            return Integer.MIN_VALUE;
        }
        return (ch2 << 8) + (ch1 << 0);
    }

}

//自行将此类提出来为public class
class QQScelMdel {

    private List<String> wordList;

    private String name;
    private String type;
    private String description;
    private String sample;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public List<String> getWordList() {
		return wordList;
	}

	public void setWordList(List<String> wordList) {
		this.wordList = wordList;
	}
}