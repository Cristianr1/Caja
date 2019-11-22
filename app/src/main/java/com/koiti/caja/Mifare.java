package com.koiti.caja;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;


public class Mifare {
    public static final int KEY_TYPE_A = 0;
    public static final int KEY_TYPE_B = 1;

    public static final int TAG_NO_COMPATIBLE = -1;
    public static final int MIFARE_CONNECTION_SUCCESS = 0;
    public static final int MIFARE_ERROR = -2;
    public static final int MIFARE_TAG_NULL = -3;

    public static final byte [] STANDAR_KEY = new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
    public static final byte [] KOITI_KEY1 = new byte []{(byte)0x33,(byte)0x56,(byte)0x30,(byte)0x70,(byte)0x34,(byte)0x72};//3V0p4r
    public static final byte [] KOITI_KEY2 = new byte []{(byte)0x41,(byte)0x63,(byte)0x53,(byte)0x45,(byte)0x76,(byte)0x50};//AcSEvP

    private Tag nTag = null;
    private MifareClassic mifareClassic = null;

    /**
     * Constructor
     * @param tag
     */
    public Mifare(Tag tag){
        mifareClassic = null;
        nTag = tag;
    }

    /**
     It will try to connect only if the tag is compatible (if it belongs to MifareClassicClass)
     @return MIFARE_TAG_NULL if the object is null,
     TAG_NO_COMPATIBLE if the tag is not compatible with Mifare,
     MIFARE_CONNECTION_SUCCESS for success connection.
     */

    public int connectTag(){
        int cont = 0;
        if(nTag == null){
            return MIFARE_TAG_NULL;
        }
        String []listaTag = nTag.getTechList();
        for(; cont < listaTag.length; cont++) {
            if (listaTag[cont].equals(MifareClassic.class.getName())){
                break;
            }
        }
        if(cont >= listaTag.length){
            return TAG_NO_COMPATIBLE;
        }
        mifareClassic = MifareClassic.get(nTag);
        try {
            mifareClassic.connect();
        }catch (IOException e) {
            e.printStackTrace();
            return MIFARE_ERROR;
        }
        return MIFARE_CONNECTION_SUCCESS;
    }

    public void disconnectTag(){

        try {
            mifareClassic.close();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     Authentificate the sector access
     @param key Array of bytes that contains the access key (6 bytes)
     @param typeKey Type of key; KEY_TYPE_A o KEY_TYPE_B
     @param sector number (0-15)
     @return true for success
     */
    public boolean authentificationKey(byte []key, int typeKey, int sector) {

        try {
            switch(typeKey) {
                case KEY_TYPE_A: if(mifareClassic.authenticateSectorWithKeyA(sector, key)) return true;
                case KEY_TYPE_B: if(mifareClassic.authenticateSectorWithKeyB(sector, key)) return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     Read a block from the especific sector
     @param sector Sector index
     @param block Block Index
     @return It returns an Array of 16 bytes or null if an error ocurrs
     */
    public byte[] readMifareTagBlock(int sector, int block){

        try {
            return mifareClassic.readBlock(block + (sector * 4));
        }catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     Write in a block from the especific sector
     @param sector Sector Index
     @param block Block Index
     @param dBuffer Array of bytes with 16 bytes of data in Hexadecimal format
     @return It retuns true for success, false otherwise
     */
    public boolean writeMifareTag(int sector,int block,byte []dBuffer){

        try {
            mifareClassic.writeBlock(block + (sector * 4), dBuffer);
        }catch(IOException e) {
            Log.e("mifare", String.valueOf(e));
            e.printStackTrace();
            return false;
        }

        return true;
    }
}