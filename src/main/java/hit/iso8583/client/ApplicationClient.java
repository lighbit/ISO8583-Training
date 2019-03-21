package hit.iso8583.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationClient {
    public static void main(String[] args) {
        // untuk Date
        DateFormat formaterbit7 = new SimpleDateFormat("MMddHHmmSS");

        // TODO: REQUEST
        /**
         * Menggunakan LinkHasMap agar bisa terurut.
         */
        /**
         * TODO: bit 7 Transmision Date and Time,
         * TODO: bit 11 Stan (System Trace Audit Number),
         * TODO: bit 70 Network Management Information Code etc....
         */
        Map<Integer, String> logonRequest = new LinkedHashMap<Integer, String>();
        logonRequest.put(7, formaterbit7.format(new Date()));
        logonRequest.put(11, "834624");
        logonRequest.put(70, "001");


        // TODO: Panggil Class lalu masukan logonrequest ke bitmap
        ApplicationClient client = new ApplicationClient();
        BigInteger bitmapRequest = client.hitungBitMap(logonRequest);

        // TODO: dikeluarkan hasilnya bitmapnya
        String strBitMap = bitmapRequest.toString(2); // 2 adalah basis bit
        System.out.println("Send Bit!");
        System.out.println("Binary: [" + strBitMap + "]");

        // TODO: di Convert ke HEXADESIMAL
        String strBitHex = bitmapRequest.toString(16); // 16 adalah basis hexa
        System.out.println("Hexa [" + strBitHex + "]");

        // TODO: Didapat Request
        String strLogonRequest = client.messageString("0800", logonRequest);
        System.out.println("Request [" + strLogonRequest + "]");

        short messageLength = (short) (strLogonRequest.length() + 2);
        System.out.println("Length [" + messageLength + "]");

//        byte[] balength = new byte[2];
//        balength[0] = (byte) ((messageLength >> 8) & 0xff);
//        balength[1] = (byte) (messageLength & 0xff);
//        System.out.println("Message Leght by order: " + new String(balength));

        // TODO: RESPONSE
        Map<Integer, String> logonResponse = new LinkedHashMap<Integer, String>();
        logonResponse.put(7, formaterbit7.format(new Date()));
        logonResponse.put(11, "000002");
        logonResponse.put(39, "00");
        logonResponse.put(70, "001");

        BigInteger bitmapResponse = client.hitungBitMap(logonResponse);
        System.out.println("Receive Bit!");
        System.out.println("\nBinary [" + bitmapResponse.toString(2) + "]");
        System.out.println("Hexa: [" + bitmapResponse.toString(16) + "]");

        client.kirim(strLogonRequest);

    }

    public BigInteger hitungBitMap(Map<Integer, String> message) {
        // TODO: Menghitung bitmap
        BigInteger bitmap = BigInteger.ZERO;
        /**
         * dikurangi satu karena bit 128 itu 1 dan bit 64 itu 0 maka harus
         * dikurangi 1
         */
        for (Integer de : message.keySet()) {
            if (de > 64) {
                bitmap = bitmap.setBit(128 - 1);
            }
            bitmap = bitmap.setBit(128 - de);

        }

        // TODO: Menghitung bitmap
        // bitmap = bitmap.setBit(128 - 7 + 1); // dikurangi satu karena bit 128
        // // itu 1 dan bit 64 itu 0 maka
        // // harus dikurangi 1
        // bitmap = bitmap.setBit(128 - 11 + 1);
        // bitmap = bitmap.setBit(128 - 70 + 1);

        return bitmap;
    }

    public String messageString(String mti, Map<Integer, String> message) {
        // TODO: dapatkan Message dengan cara mti + hitungbitMap
        StringBuilder hasil = new StringBuilder();
        hasil.append(mti);
        hasil.append(hitungBitMap(message).toString(16));
        for (Integer de : message.keySet()) {
            hasil.append(message.get(de));
        }
        return hasil.toString();
    }

    public void kirim(String message) {
        short messageLength = (short) (message.length() + 2);
        System.out.println("Length [" + messageLength + "]");

        try {
            // TODO: Mengirim Data
            Socket koneksi = new Socket("localhost", 8000);
            DataOutputStream out = new DataOutputStream(koneksi.getOutputStream());
            out.writeShort(messageLength);
            out.writeBytes(message);
            out.flush();
            System.out.println("Data Send! and you got: ");

            // Menerima Response
            DataInputStream in = new DataInputStream(koneksi.getInputStream());
            short respLength = in.readShort();
            System.out.println("Length Response [" + respLength + "]");
            // dipotong respLength nya
            byte[] responseData = new byte[respLength - 2];
            in.readFully(responseData);
            System.out.println("Response [" + new String(responseData) + "]");


            in.close();
            out.close();
            System.out.println("message fully received");

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

}
