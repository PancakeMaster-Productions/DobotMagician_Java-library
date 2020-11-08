package app;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import jssc.*;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Formatter;

public class Dobot
{
    /**
     * DOBOT Magician library for Java and JavaFX.
     * Experimental! Conversion needed before using!
     * Use protocol-manual before using!
     *
     * Credits: jacquesjpjohnston.
     * Documentation src: 'https://jacquesjohnston.files.wordpress.com/2019/01/dobot-magician-communication-protocol-v1.1.3.pdf'.
     */

    private String start_bytes = "AAAA";
//    private static jssc.SerialPort serialPort = null;
//    private static String dobot_hostname = "192.168.0.101";
//    private static int dobot_port = 8899;
//    private static int dobot_time_out = 200;
//    private static int after_send_sleep = 20;

    private jssc.SerialPort serialPort = null;
    private String dobot_serial_port = "\u001B[31mnull\u001B[0m";
    private String dobot_hostname = "\u001B[31mnull\u001B[0m";
    private int dobot_port = 8899;
    private int dobot_time_out = 200;
    private int after_send_sleep = 20;
    private boolean debug_mode = false;
    private String debug_mode_tag = "\u001B[33m[DEBUG MODE]\u001B[0m";
    private String error_cant_parse = "\u001B[31m[ERROR]\u001B[0m Cannot parse incoming data from socket, connection is probably down or dobot action request is to fast after the other request!";

    /**
     * DOBOT information - commands.
     */

    public String GetAlarmState()
    {
        String payload = "140000";
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String GetDeviceTime_exp()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03040000FC");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String GetDeviceID_exp()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03040000FC");
        System.out.println(reply);
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String GetDeviceVersion()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA020200FE");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }

        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String GetDeviceName()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA020100FF");
        try
        {
            // Remove 00 from reply.
            reply = reply.replace("00", "");
            // Remove id and checksum from reply.
            reply = reply.substring(8, reply.length()-2);
            // Convert hex string to UTF-8.
            reply = new String(decodeHexStringToHex(reply), "UTF-8");
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return reply;
    }

    public void SetDeviceName(String device_name)
    {
        // Convert device_name to hex String.
        device_name = byteToHex(device_name.getBytes());
        // Add users request to payload.
        String payload = "0100" + device_name;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public boolean GetDeviceWithL()
    {
        Boolean state = false;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03030000FD");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // If state equals '01' return true.
        if (reply.equals("01")) state = true;
        return state;
    }

    public void SetDeviceWithL(boolean enable_sliding_rails)
    {
        String string_enable_sliding_rails = "00";
        if (enable_sliding_rails == true) string_enable_sliding_rails = "01";
        // Add users request to payload.
        String payload = "0301" + string_enable_sliding_rails;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    // TODO: Werking is onbekend.
    public void ClearAllAlarmsState()
    {
        String payload = "150100";
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT and send payload.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetHOMEParams(double x, double y, double z, double r)
    {
        // Convert device_name to hex String.
        String string_x = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase());
        String string_y = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase());
        String string_z = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase());
        String string_r = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(r))).toUpperCase());

        // Add users request to payload.
        String payload = "1E0100" + string_x + string_y + string_z + string_r;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetHOMEParams()
    {
        double coordinates[];
        String payload = "1E0000";
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
        // Get the coordinates.
        coordinates = get_coordinates_from_payload(reply);
        return coordinates;
    }

    public void SetHomeCmd()
    {
        connect_Dobot("AAAA061F0300000000DE");
    }

    public void SetQueuedCmdForceStopExec()
    {
        connect_Dobot("AAAA02f2010d");
    }

    public String GetHHTTrigOutput()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA0200d6");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    /**
     *  WIFI commands.
     */

    public String GetWifiGateway()
    {
        String gateway = "";
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA039B000065");
        /// Get payload from reply with using 'len' as payload size.
        try {
            Thread.sleep(200);
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));

            for (int i = 0; i < 4; i++)
            {
                // Convert hex to decimal and create a netmask String.
                gateway = gateway + "." + Long.parseLong(reply.substring(0 + (i * 2), 2 + (i * 2)), 16);
            }
            // Remove first '.' from netmask.
            gateway = gateway.substring(1);
        } catch (Exception e) {
            System.out.println(error_cant_parse);
        }
        return gateway;
    }

    public String SetWifiGateway_exp(String wifi_gateway)
    {
        // Remove '.' from wifi_gateway.
        wifi_gateway = wifi_gateway.replace(".", "");
        // Convert device_name to hex String.
        wifi_gateway = byteToHex(wifi_gateway.getBytes());
        String payload = "9B0100" + wifi_gateway;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public boolean GetWifiConfigMode()
    {
        boolean state = false;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA039600006A");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(8, 7 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + ""));
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Remove id from reply.
        if (reply.contains("01")) state = true;
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public void SetWifiConfigMode(boolean disable_wifi)
    {
        String string_enable_wifi = "00";
        if (disable_wifi == true) string_enable_wifi = "01";
        // Add users request to payload.
        String payload = "9601" + string_enable_wifi;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public String GetWIFIIPAddress()
    {
        String ip_address = "";
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA0399000067");
        // Get payload from reply with using 'len' as payload size.
        try {
            Thread.sleep(200);
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));

            for (int i = 1; i < 5; i++)
            {
                // Convert hex to decimal and create a netmask String.
                ip_address = ip_address + "." + Long.parseLong(reply.substring(0 + (i * 2), 2 + (i * 2)), 16);
            }
            // Remove first '.' from netmask.
            ip_address = ip_address.substring(1);
        } catch (Exception e) {
            System.out.println(error_cant_parse);
        }
        return ip_address;
    }

    public void SetWIFIIPAddress_exp(String wifi_ip)
    {
        // Remove '.' from wifi_ip.
        wifi_ip = wifi_ip.replace(".", "");
        // Convert device_name to hex String.
        wifi_ip = byteToHex(wifi_ip.getBytes());
        String payload = "990100" + wifi_ip;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public boolean GetWIFIConnectStatus()
    {
        boolean state = false;
        String payload = "9D0000";
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
        try {
            /// Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, reply.length() - 2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        if (reply.contains("01")) state = true;
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public String GetWIFIDNS()
    {
        String dns = "";
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA039C000064");
        try {
            Thread.sleep(200);
            System.out.println(reply);
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));

            for (int i = 0; i < 4; i++)
            {
                // Convert hex to decimal and create a netmask String.
                dns = dns + "." + Long.parseLong(reply.substring(0 + (i * 2), 2 + (i * 2)), 16);
            }
            // Remove first '.' from netmask.
            dns = dns.substring(1);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(error_cant_parse);
        }
        return dns;
    }

    public void SetWIFIDNS_exp(String wifi_dns)
    {
        // Remove '.' from wifi_ip.
        wifi_dns = wifi_dns.replace(".", "");
        // Convert device_name to hex String.
        wifi_dns = byteToHex(wifi_dns.getBytes());
        // Add users request to payload.
        String payload = "9C01" + wifi_dns;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public String GetWIFINetmask()
    {
        String netmask = "";
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA029A0066");
        // Get payload from reply with using 'len' as payload size.
        try {
            Thread.sleep(200);
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));

            for (int i = 0; i < 4; i++)
            {
                // Convert hex to decimal and create a netmask String.
                netmask = netmask + "." + Long.parseLong(reply.substring(0 + (i * 2), 2 + (i * 2)), 16);
            }
            // Remove first '.' from netmask.
            netmask = netmask.substring(1);
        } catch (Exception e) {
            System.out.println(error_cant_parse);
        }
        return netmask;
    }

    public void SetWIFINetmask_exp(String wifi_netmask)
    {
        // Remove '.' from wifi_ip.
        wifi_netmask = wifi_netmask.replace(".", "");
        // Convert device_name to hex String.
        wifi_netmask = byteToHex(wifi_netmask.getBytes());
        // Add users request to payload.
        String payload = "9A01" + wifi_netmask;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public String GetWIFIPassword()
    {
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA02980068");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(8, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Convert hex string to UTF-8.
            reply = new String(decodeHexStringToHex(reply), "UTF-8");
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        after_send_sleep = 20;
        return reply;
    }

    public void SetWIFIPassword(String wifi_password)
    {
        // Convert device_name to hex String.
        wifi_password = byteToHex(wifi_password.getBytes());
        // Add users request to payload.
        String payload = "9801" + wifi_password;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public String GetWIFISSID()
    {
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA02970069");
        // Get payload from reply with using 'len' as payload size.
        try {
            Thread.sleep(200);
            reply = reply.substring(8, 6 + (Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) + "") * 2));
            reply = new String(decodeHexStringToHex(reply), "UTF-8");
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        after_send_sleep = 20;
        return reply;
    }

    public void SetWIFISSID(String wifi_ssid)
    {
        // Convert device_name to hex String.
        wifi_ssid = byteToHex(wifi_ssid.getBytes());
        // Add users request to payload.
        String payload = "9701" + wifi_ssid;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     *  Queued execution control commands.
     */

    public String GetQueuedCmdCurrentIndex()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03F600000A");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply and rw.
            reply = reply.substring(4);
            reply = Long.parseLong(hex_lsb(reply), 16) + "";
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String GetQueuedCmdLeftSpace()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03F7000009");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply and rw.
            reply = reply.substring(4);
            reply = Long.parseLong(hex_lsb(reply), 16) + "";
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public void SetQueuedCmdStopDownload()
    {
        connect_Dobot("AAAA03F400000C");
    }

    public void SetQueuedCmdClear()
    {
        connect_Dobot("AAAA03F500000B");
    }

    public void SetQueuedCmdStartExec()
    {
        connect_Dobot("AAAA03F0000010");
    }

    public void SetQueuedCmdStopExec()
    {
        connect_Dobot("AAAA03F600000A");
    }

    public void SetQueuedCmdStartDownload(int total_loop, int line_per_loop)
    {
        // Convert total_loop to hex String.
        String string_total_loop = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(total_loop))).toUpperCase());
        // Convert line_per_loop to hex String.
        String string_line_per_loop = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(line_per_loop))).toUpperCase());
        // Add users request to payload.
        String payload = "F30100" + string_total_loop + string_line_per_loop;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * Handhold Teaching.
     */

    public String GetHHTTrigMode_exp()
    {
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03280000D8");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public String SetHHTTrigMode_exp(String HHTTrigMode)
    {
        // Convert device_name to hex String.
        HHTTrigMode = byteToHex(HHTTrigMode.getBytes());
        // Add users request to payload.
        String payload = "280100" + HHTTrigMode;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
        System.out.println(reply);
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(6, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            reply = reply.substring(2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return reply;
    }

    public boolean GetHHTTrigOutputEnabled()
    {
        boolean state = false;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA03290000D7");
        try {
            /// Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, reply.length() - 2);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Remove id from reply.
        if (reply.equals("01")) state = true;
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public void SetHHTTrigOutputEnabled(boolean is_auto_leveling)
    {
        String string_is_auto_leveling = "00";
        if (is_auto_leveling == true) string_is_auto_leveling = "01";
        // Add users request to payload.
        String payload = "1E0100" + string_is_auto_leveling;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * Losing-Step Detection.
     */

    public void SetLostStepParams(double step_value)
    {
        // Convert device_name to hex String.
        String string_step_value = Integer.toHexString(Float.floatToIntBits(new Float(step_value))).toUpperCase();
        // Add users request to payload.
        String payload = "AA0100" + string_step_value;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetLostStepCmd()
    {
        connect_Dobot("AAAA03AB000055");
    }

    /**
     * EndEffector.
     */

    public void SetEndEffectorParams(double xBias, double yBias, double zBias)
    {
        // Convert device_name to hex String.
        String string_xBias = Integer.toHexString(Float.floatToIntBits(new Float(xBias))).toUpperCase();
        String string_yBias = Integer.toHexString(Float.floatToIntBits(new Float(yBias))).toUpperCase();
        String string_zBias = Integer.toHexString(Float.floatToIntBits(new Float(zBias))).toUpperCase();
        // Add users request to payload.
        String payload = "3C0100" + string_xBias + string_yBias + string_zBias;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetEndEffectorParams()
    {
        double coordinates[] = {0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA033C0000C4");
        // Get the coordinates.
        System.out.println("GOT: " + reply);
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println("GOT 2: " + reply);
            // Get float value from reply.
            coordinates[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            coordinates[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
            coordinates[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 12)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return coordinates;
    }

    public void SetEndEffectorLaser(boolean enable_ctrl, boolean is_enabled)
    {
        String string_enable_ctrl = "00";
        String string_is_enabled = "00";
        if (enable_ctrl == true) string_enable_ctrl = "01";
        if (is_enabled == true) string_is_enabled = "01";

        String payload = "3D01" + string_enable_ctrl + string_is_enabled;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT and send payload.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public boolean[] GetEndEffectorLaser()
    {
        boolean state[] = {false, false};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA033D0000C3");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            if (reply.substring(0, 2).equals("01")) state[0] = true;
            if (reply.substring(2, 4).equals("01")) state[1] = true;
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public void SetEndEffectorSuctionCup(boolean enable_ctrl, boolean is_sucked)
    {
        String string_enable_ctrl = "00";
        String string_is_sucked = "00";
        if (enable_ctrl == true) string_enable_ctrl = "01";
        if (is_sucked == true) string_is_sucked = "01";

        String payload = "3E01" + string_enable_ctrl + string_is_sucked;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT and send payload.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public boolean[] GetEndEffectorSuctionCup()
    {
        boolean state[] = {false, false};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA033E0000C2");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            if (reply.substring(0, 2).equals("01")) state[0] = true;
            if (reply.substring(2, 4).equals("01")) state[1] = true;
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public void SetEndEffectorGripper(boolean enable_vacuum, boolean gripper_closed)
    {
        String string_enable_vacuum = "00";
        String string_gripper_open = "00";
        if (enable_vacuum == true) string_enable_vacuum = "01";
        if (gripper_closed == true) string_gripper_open = "01";

        String payload = "3F01" + string_enable_vacuum + string_gripper_open;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        /*System.out.println(start_bytes + len_hex + payload + checkSum_hex);*/
        // Connect with DOBOT and send payload.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public boolean[] GetEndEffectorGripper()
    {
        boolean state[] = {false, false};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA033F0000C1");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            if (reply.substring(0, 2).equals("01")) state[0] = true;
            if (reply.substring(2, 4).equals("01")) state[1] = true;
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return state;
    }

    public double GetAutoLeveling()
    {
        float auto_leveling_val = 0F;
        // This commands takes longer to execute change after_send_sleep to 2000 milliseconds.
        after_send_sleep = 2000;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA031E0000E2");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(8, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            auto_leveling_val = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(2, 10)), 16));
        }
        catch (Exception e)
        {
            System.out.println(e);
            System.out.println(error_cant_parse);
        }
        after_send_sleep = 20;
        return auto_leveling_val;
    }

    public void SetAutoLeveling(boolean is_auto_leveling, double accuracy)
    {
        String string_is_auto_leveling = "00";
        if (is_auto_leveling == true) string_is_auto_leveling = "01";
        // Convert device_name to hex String.
        String string_accuracy = Integer.toHexString(Float.floatToIntBits(new Float(accuracy))).toUpperCase();
        // Add users request to payload.
        String payload = "1E0100" + string_is_auto_leveling + string_accuracy;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * Real-time Pose.
     */

    // TODO: Joints
    public double[] GetPose()
    {
        double coordinates[];
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA030A0001F5");
        // Get the coordinates.
        coordinates = get_coordinates_from_payload(reply);
        return coordinates;
    }

    public void ResetPose()
    {
        connect_Dobot("AAAA030B0001F4");
    }

    public double GetPoseL()
    {
        float pose_l = 0F;
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        String reply = connect_Dobot("AAAA030D0001F2");


            System.out.println("GOT: " + reply);
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 18);
            // Get float value from reply.
            pose_l = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply), 16));

        return pose_l;
    }

    /**
     * Calibration (CAL).
     */

    public double[] GetAngleSensorStaticError()
    {
        double error[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038C000074");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            // Get float value from reply.
            error[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            error[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return rearArmAngle and frontArmAngle error.
        return error;
    }

    public void SetAngleSensorStaticError(double rearArmAngle_error, double frontArmAngle_error)
    {
        // Convert device_name to hex String.
        String string_rearArmAngle_error = Integer.toHexString(Float.floatToIntBits(new Float(rearArmAngle_error))).toUpperCase();
        String string_frontArmAngle_error = Integer.toHexString(Float.floatToIntBits(new Float(frontArmAngle_error))).toUpperCase();
        // Add users request to payload.
        String payload = "8C0100" + string_rearArmAngle_error + string_frontArmAngle_error;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * JOG.
     */

    public void SetJOGJointParams(double velocity_1, double velocity_2, double velocity_3, double velocity_4, double acceleration_1, double acceleration_2, double acceleration_3, double acceleration_4)
    {
        // Convert device_name to hex String.
        String string_velocity_1 = Integer.toHexString(Float.floatToIntBits(new Float(velocity_1))).toUpperCase();
        String string_velocity_2 = Integer.toHexString(Float.floatToIntBits(new Float(velocity_2))).toUpperCase();
        String string_velocity_3 = Integer.toHexString(Float.floatToIntBits(new Float(velocity_3))).toUpperCase();
        String string_velocity_4 = Integer.toHexString(Float.floatToIntBits(new Float(velocity_4))).toUpperCase();
        String string_acceleration_1 = Integer.toHexString(Float.floatToIntBits(new Float(acceleration_1))).toUpperCase();
        String string_acceleration_2 = Integer.toHexString(Float.floatToIntBits(new Float(acceleration_2))).toUpperCase();
        String string_acceleration_3 = Integer.toHexString(Float.floatToIntBits(new Float(acceleration_3))).toUpperCase();
        String string_acceleration_4 = Integer.toHexString(Float.floatToIntBits(new Float(acceleration_4))).toUpperCase();
        // Add users request to payload.
        String payload = "460100" + string_velocity_1 + string_velocity_2 + string_velocity_3 + string_velocity_4 + string_acceleration_1 + string_acceleration_2 + string_acceleration_3 + string_acceleration_4;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetJOGJointParams()
    {
        double values[] = {0, 0, 0, 0, 0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03460000BA");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 12)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(12, 20)), 16));
            values[4] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(20, 28)), 16));
            values[5] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(28, 36)), 16));
            values[6] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(36, 44)), 16));
            values[7] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(44, 52)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return values;
    }

    public void SetJOGCoordinateParams(double x_velocity, double y_velocity, double z_velocity, double r_velocity, double x_acceleration, double y_acceleration, double z_acceleration, double r_acceleration)
    {
        // Convert device_name to hex String.
        String string_x_velocity = Integer.toHexString(Float.floatToIntBits(new Float(x_velocity))).toUpperCase();
        String string_y_velocity = Integer.toHexString(Float.floatToIntBits(new Float(y_velocity))).toUpperCase();
        String string_z_velocity = Integer.toHexString(Float.floatToIntBits(new Float(z_velocity))).toUpperCase();
        String string_r_velocity = Integer.toHexString(Float.floatToIntBits(new Float(r_velocity))).toUpperCase();
        String string_x_acceleration = Integer.toHexString(Float.floatToIntBits(new Float(x_acceleration))).toUpperCase();
        String string_y_acceleration = Integer.toHexString(Float.floatToIntBits(new Float(y_acceleration))).toUpperCase();
        String string_z_acceleration = Integer.toHexString(Float.floatToIntBits(new Float(z_acceleration))).toUpperCase();
        String string_r_acceleration = Integer.toHexString(Float.floatToIntBits(new Float(r_acceleration))).toUpperCase();
        // Add users request to payload.
        String payload = "470100" + string_x_velocity + string_y_velocity + string_z_velocity + string_r_velocity + string_x_acceleration + string_y_acceleration + string_z_acceleration + string_r_acceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetJOGCoordinateParams()
    {
        double values[] = {0, 0, 0, 0, 0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03470000B9");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 12)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(12, 20)), 16));
            values[4] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(20, 28)), 16));
            values[5] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(28, 36)), 16));
            values[6] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(36, 44)), 16));
            values[7] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(44, 52)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return values;
    }

    public void SetJOGCommonParams(double velocityRatio, double accelerationRatio)
    {
        // Convert device_name to hex String.
        String string_velocityRatio = Integer.toHexString(Float.floatToIntBits(new Float(velocityRatio))).toUpperCase();
        String string_accelerationRatio = Integer.toHexString(Float.floatToIntBits(new Float(accelerationRatio))).toUpperCase();
        // Add users request to payload.
        String payload = "480100" + string_velocityRatio + string_accelerationRatio;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetJOGCommonParams()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03480000B8");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return values;
    }

    public void SetSetJOGCmd_exp(boolean isJoint, boolean cmd)
    {
        String string_isJoint = "00";
        if (isJoint == true) string_isJoint = "01";
        String string_cmd = "00";
        if (cmd == true) string_cmd = "01";
        // Add users request to payload.
        String payload = "490100" + string_isJoint + string_cmd;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetJOGLParams()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA034A0000B6");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        return values;
    }

    public void SetJOGLParams(double velocity, double acceleration)
    {
        // Convert device_name to hex String.
        String string_velocity = Integer.toHexString(Float.floatToIntBits(new Float(velocity))).toUpperCase();
        String string_acceleration = Integer.toHexString(Float.floatToIntBits(new Float(acceleration))).toUpperCase();
        // Add users request to payload.
        String payload = "4A0100" + string_velocity + string_acceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * EIO.
     */

    public int[] GetIOMultiplexing()
    {
        int value[] = {0 , 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA05820100020378");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            value[0] = (int)Long.parseLong(reply.substring(0, 2), 16);
            value[1] = (int)Long.parseLong(reply.substring(2, 4), 16);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return value;
    }

    public void SetIOMultiplexing(int EIO_addressing, int EIO_function)
    {
        String string_EIO_addressing = Long.parseLong(EIO_addressing + "", 16) + "";
        if (string_EIO_addressing.length()<2) string_EIO_addressing = "0" + string_EIO_addressing;
        String string_EIO_function = Long.parseLong(EIO_function + "", 16) + "";
        if (string_EIO_function.length()<2) string_EIO_function = "0" + string_EIO_function;
        // Add users request to payload.
        String payload = "820100" + string_EIO_addressing + string_EIO_function;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public int[] GetIODO()
    {
        int value[] = {0 , 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038300007D");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            value[0] = (int)Long.parseLong(reply.substring(0, 2), 16);
            value[1] = (int)Long.parseLong(reply.substring(2, 4), 16);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return value;
    }

    public void SetIODO(int EIO_addressing, int EIO_function)
    {
        String string_EIO_addressing = Long.parseLong(EIO_addressing + "", 16) + "";
        if (string_EIO_addressing.length()<2) string_EIO_addressing = "0" + string_EIO_addressing;
        String string_EIO_function = Long.parseLong(EIO_function + "", 16) + "";
        if (string_EIO_function.length()<2) string_EIO_function = "0" + string_EIO_function;
        // Add users request to payload.
        String payload = "830100" + string_EIO_addressing + string_EIO_function;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetIOPWM()
    {
        double values[] = {0 , 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038400007C");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            values[0] = Long.parseLong(reply.substring(0, 2), 16);
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return values;
    }

    public void SetIOPWM(int EIO_addressing, double PWM_frequency, double PWM_duty_ratio)
    {
        String string_EIO_addressing = Long.parseLong(EIO_addressing + "", 16) + "";
        if (string_EIO_addressing.length() < 2) string_EIO_addressing = "0" + string_EIO_addressing;
        String string_WM_frequency = Integer.toHexString(Float.floatToIntBits(new Float(PWM_frequency))).toUpperCase();
        String string_PWM_duty_ratio = Integer.toHexString(Float.floatToIntBits(new Float(PWM_duty_ratio))).toUpperCase();
        // Add users request to payload.
        String payload = "840100" + string_EIO_addressing + string_WM_frequency + string_PWM_duty_ratio;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public int[] GetIODI()
    {
        int value[] = {0 , 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038500007B");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            value[0] = (int)Long.parseLong(reply.substring(0, 2), 16);
            value[1] = (int)Long.parseLong(reply.substring(2, 4), 16);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return value;
    }

    public double[] GetIOADC()
    {
        double values[] = {0 , 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038600007A");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            values[0] = Long.parseLong(reply.substring(0, 2), 16);
            values[1] = Long.parseLong(reply.substring(2, 6), 16);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return uint8_t[16]:alarmsState.
        return values;
    }

    public void SetEMotor(int index_stepper, boolean is_motor_enabled, double amount_of_pulses)
    {
        String string_index_stepper = Long.parseLong(index_stepper + "", 16) + "";
        if (string_index_stepper.length() < 2) string_index_stepper = "0" + string_index_stepper;
        String string_is_motor_enabled = "00";
        if (is_motor_enabled == true) string_is_motor_enabled = "01";
        String string_amount_of_pulses = Integer.toHexString(Float.floatToIntBits(new Float(amount_of_pulses))).toUpperCase();
        // Add users request to payload.
        String payload = "870100" + string_index_stepper + string_is_motor_enabled + string_amount_of_pulses;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public int[] GetColorSensor()
    {
        int value[] = {0 , 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA0389000077");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            value[0] = (int)Long.parseLong(reply.substring(0, 2), 16);
            value[1] = (int)Long.parseLong(reply.substring(2, 4), 16);
            value[2] = (int)Long.parseLong(reply.substring(4, 6), 16);
        }
        catch (Exception ex)
        {
            System.out.println(error_cant_parse);
        }
        // Return red, green and blue (R,G,B).
        return value;
    }

    public void SetColorSensor(int PORT_GP1, int PORT_GP2, int PORT_GP3, int PORT_GP4)
    {
        String string_PORT_GP1 = Long.parseLong(PORT_GP1 + "", 16) + "";
        if (string_PORT_GP1.length()<2) string_PORT_GP1 = "0" + string_PORT_GP1;
        String string_PORT_GP2 = Long.parseLong(PORT_GP2 + "", 16) + "";
        if (string_PORT_GP2.length()<2) string_PORT_GP2 = "0" + string_PORT_GP2;
        String string_PORT_GP3 = Long.parseLong(PORT_GP3 + "", 16) + "";
        if (string_PORT_GP3.length()<2) string_PORT_GP3 = "0" + string_PORT_GP3;
        String string_PORT_GP4 = Long.parseLong(PORT_GP4 + "", 16) + "";
        if (string_PORT_GP4.length()<2) string_PORT_GP4 = "0" + string_PORT_GP4;
        // Add users request to payload.
        String payload = "890100" + string_PORT_GP1 + string_PORT_GP2 + string_PORT_GP3 + string_PORT_GP4;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public int GetIRSwitch()
    {
        int values = 0;
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA038A000076");
        try {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6 + Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16) * 2 + ""));
            // Remove id from reply.
            values = (int)Long.parseLong(reply.substring(0, 2), 16);
        }
        catch (Exception ex)
        {
            System.out.println(ex);
            System.out.println(error_cant_parse);
        }
        // Return state.
        return values;
    }

    public void SetIRSwitch(int IR_enabled, int IRPort)
    {
        String string_IR_enabled = Long.parseLong(IR_enabled + "", 16) + "";
        if (string_IR_enabled.length()<2) string_IR_enabled = "0" + string_IR_enabled;
        String string_IRPort = Long.parseLong(IRPort + "", 16) + "";
        if (string_IRPort.length()<2) string_IRPort = "0" + string_IRPort;
        // Add users request to payload.
        String payload = "890100" + string_IR_enabled + string_IRPort;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * TRIG.
     */

    public void SetTRIGCmd(int address, int mode, int condition, int threshold)
    {
        String string_address = Long.parseLong(address + "", 16) + "";
        if (string_address.length()<2) string_address = "0" + string_address;
        String string_mode = Long.parseLong(mode + "", 16) + "";
        if (string_mode.length()<2) string_mode = "0" + string_mode;
        String string_condition = Long.parseLong(condition + "", 16) + "";
        if (string_condition.length()<2) string_condition = "0" + string_condition;
        String string_threshold = Long.parseLong(threshold + "", 16) + "";
        if (string_threshold.length()<4) while (string_threshold.length()<4) string_threshold = "0" + string_threshold;
        // Add users request to payload.
        String payload = "780100" + string_address + string_mode + string_condition + string_threshold;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * WAIT.
     */

    public void SetWAITCmd(int timeout)
    {
        String string_timeout = Long.parseLong(timeout + "", 16) + "";
        if (string_timeout.length()<4) while (string_timeout.length()<4) string_timeout = "0" + string_timeout;
        // Add users request to payload.
        String payload = "780100" + string_timeout;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * ARC.
     */

    public double[] GetARCParams()
    {
        double values[] = {0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA036400009C");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 12)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(12, 16)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetARCParams(double xyzVelocity, double rVelocity, double xyzAcceleration, double rAcceleration)
    {
        // Convert device_name to hex String.
        String string_xyzVelocity = Integer.toHexString(Float.floatToIntBits(new Float(xyzVelocity))).toUpperCase();
        String string_rVelocity = Integer.toHexString(Float.floatToIntBits(new Float(rVelocity))).toUpperCase();
        String string_xyzAcceleration = Integer.toHexString(Float.floatToIntBits(new Float(xyzAcceleration))).toUpperCase();
        String string_rAcceleration = Integer.toHexString(Float.floatToIntBits(new Float(rAcceleration))).toUpperCase();
        // Add users request to payload.
        String payload = "640100" + string_xyzVelocity + string_rVelocity + string_xyzAcceleration + string_rAcceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetARCCmd(double x1, double y1, double z1, double r1, double x2, double y2, double z2, double r2)
    {
        // Convert device_name to hex String.
        String string_x1 = Integer.toHexString(Float.floatToIntBits(new Float(x1))).toUpperCase();
        String string_y1 = Integer.toHexString(Float.floatToIntBits(new Float(y1))).toUpperCase();
        String string_z1 = Integer.toHexString(Float.floatToIntBits(new Float(z1))).toUpperCase();
        String string_r1 = Integer.toHexString(Float.floatToIntBits(new Float(r1))).toUpperCase();
        String string_x2 = Integer.toHexString(Float.floatToIntBits(new Float(x2))).toUpperCase();
        String string_y2 = Integer.toHexString(Float.floatToIntBits(new Float(y2))).toUpperCase();
        String string_z2 = Integer.toHexString(Float.floatToIntBits(new Float(z2))).toUpperCase();
        String string_r2 = Integer.toHexString(Float.floatToIntBits(new Float(r2))).toUpperCase();
        // Add users request to payload.
        String payload = "650100" + string_x1 + string_y1 + string_z1 + string_r1 + string_x2 + string_y2 + string_z2 + string_r2;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * CP.
     */

    public double[] GetCPParams()
    {
        double values[] = {0, 0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA035A0000A6");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 4)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(4, 8)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 12)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(12, 16)), 16));
            values[4] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(16, 18)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetCPParams(double planAcc, double junctionVel, double acc, double period, boolean realTimeTrack)
    {
        // Convert device_name to hex String.
        String string_planAcc = Integer.toHexString(Float.floatToIntBits(new Float(planAcc))).toUpperCase();
        String string_junctionVel = Integer.toHexString(Float.floatToIntBits(new Float(junctionVel))).toUpperCase();
        String string_acc = Integer.toHexString(Float.floatToIntBits(new Float(acc))).toUpperCase();
        String string_period = Integer.toHexString(Float.floatToIntBits(new Float(period))).toUpperCase();
        String string_realTimeTrack = "00";
        if (realTimeTrack == true) string_realTimeTrack = "01";

        // Add users request to payload.
        String payload = "5A0100" + string_planAcc + string_junctionVel + string_acc + string_period + string_realTimeTrack;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetCPCmd(boolean cpMode_enabled, double x, double y, double z, double velocity, double power)
    {
        // Convert device_name to hex String.
        String string_x = Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase();
        String string_velocity = Integer.toHexString(Float.floatToIntBits(new Float(velocity))).toUpperCase();
        String string_power = Integer.toHexString(Float.floatToIntBits(new Float(power))).toUpperCase();
        String string_cpMode_enabled = "00";
        if (cpMode_enabled == true) string_cpMode_enabled = "01";

        // Add users request to payload.
        String payload = "5B0100" + string_cpMode_enabled + string_x + string_y + string_z + string_velocity + string_power;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetCPLECmd(boolean cpMode_enabled, double x, double y, double z, double velocity, double power)
    {
        // Convert device_name to hex String.
        String string_x = Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase();
        String string_velocity = Integer.toHexString(Float.floatToIntBits(new Float(velocity))).toUpperCase();
        String string_power = Integer.toHexString(Float.floatToIntBits(new Float(power))).toUpperCase();
        String string_cpMode_enabled = "00";
        if (cpMode_enabled == true) string_cpMode_enabled = "01";

        // Add users request to payload.
        String payload = "5C0100" + string_cpMode_enabled + string_x + string_y + string_z + string_velocity + string_power;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * PTP.
     */

    public void SetPTPJointParams(double x_velocity, double y_velocity, double z_velocity, double r_velocity, double x_acceleration, double y_acceleration, double z_acceleration, double r_acceleration)
    {
        // Convert device_name to hex String.
        String string_x_velocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(x_velocity))).toUpperCase());
        String string_y_velocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(y_velocity))).toUpperCase());
        String string_z_velocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(z_velocity))).toUpperCase());
        String string_r_velocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(r_velocity))).toUpperCase());

        String string_x_acceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(x_acceleration))).toUpperCase());
        String string_y_acceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(y_acceleration))).toUpperCase());
        String string_z_acceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(z_acceleration))).toUpperCase());
        String string_r_acceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(r_acceleration))).toUpperCase());

        // Add users request to payload.
        String payload = "500100" + string_x_velocity + string_y_velocity + string_z_velocity + string_r_velocity + string_x_acceleration + string_y_acceleration + string_z_acceleration + string_r_acceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetPTPJointParams()
    {
        double values[] = {0, 0, 0, 0, 0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03500000B0");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(16, 24)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(24, 32)), 16));
            values[4] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(32, 40)), 16));
            values[5] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(40, 48)), 16));
            values[6] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(48, 56)), 16));
            values[7] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(56, 64)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public double[] GetPTPCoordinateParams()
    {
        double values[] = {0, 0, 0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03510000AF");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
            values[2] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(16, 24)), 16));
            values[3] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(24, 32)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetPTPCoordinateParams(double xyzVelocity, double rVelocity, double xyzAcceleration, double rAcceleration)
    {
        // Convert device_name to hex String.
        String string_xyzVelocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(xyzVelocity))).toUpperCase());
        String string_rVelocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(rVelocity))).toUpperCase());
        String string_xyzAcceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(xyzAcceleration))).toUpperCase());
        String string_rAcceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(rAcceleration))).toUpperCase());

        // Add users request to payload.
        String payload = "510100" + string_xyzVelocity + string_rVelocity + string_xyzAcceleration + string_rAcceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetPTPJumpParams()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03520000AE");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetPTPJumpParams(double jumpHeight, double zLimit)
    {
        // Convert device_name to hex String.
        String string_jumpHeight = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(jumpHeight))).toUpperCase());
        String string_zLimit = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(zLimit))).toUpperCase());

        // Add users request to payload.
        String payload = "520100" + string_jumpHeight + string_zLimit;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetPTPCommonParams()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03530000AD");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetPTPCommonParams(double velocityRatio, double accelerationRatio)
    {
        // Convert device_name to hex String.
        String string_velocityRatio = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(velocityRatio))).toUpperCase());
        String string_accelerationRatio = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(accelerationRatio))).toUpperCase());

        // Add users request to payload.
        String payload = "530100" + string_velocityRatio + string_accelerationRatio;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetPTPCmd(double x, double y, double z, double r)
    {
        // SetPTPJointParams.
        String ptpMode = "01";

        Float float_x = new Float(x);
        Float float_y = new Float(y);
        Float float_z = new Float(z);
        Float float_r = new Float(r);

        // Convert variables from float value to hex.
        String string_x = Integer.toHexString(Float.floatToIntBits(float_x)).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(float_y)).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(float_z)).toUpperCase();
        String string_r = Integer.toHexString(Float.floatToIntBits(float_r)).toUpperCase();
        // If coordinates = 0 change it to '00000000'.
        if (string_x.equals("0")) string_x = "00000000";
        if (string_y.equals("0")) string_y = "00000000";
        if (string_z.equals("0")) string_z = "00000000";
        if (string_r.equals("0")) string_r = "00000000";
        // Get hex byte from string_x.
        String x_byte_1 = string_x.substring(0, 2);
        String x_byte_2 = string_x.substring(2, 4);
        String x_byte_3 = string_x.substring(4, 6);
        String x_byte_4 = string_x.substring(6, 8);
        // Put bytes in least significant byte order.
        String x_bytes = x_byte_4 + x_byte_3 + x_byte_2 + x_byte_1;
        // Get hex byte from string_y.
        String y_byte_1 = string_y.substring(0, 2);
        String y_byte_2 = string_y.substring(2, 4);
        String y_byte_3 = string_y.substring(4, 6);
        String y_byte_4 = string_y.substring(6, 8);
        // Put bytes in least significant byte order.
        String y_bytes = y_byte_4 + y_byte_3 + y_byte_2 + y_byte_1;
        // Get hex byte from string_y.
        String z_byte_1 = string_z.substring(0, 2);
        String z_byte_2 = string_z.substring(2, 4);
        String z_byte_3 = string_z.substring(4, 6);
        String z_byte_4 = string_z.substring(6, 8);
        // Put bytes in least significant byte order.
        String z_bytes = z_byte_4 + z_byte_3 + z_byte_2 + z_byte_1;
        // Get hex byte from string_r.
        String r_byte_1 = string_r.substring(0, 2);
        String r_byte_2 = string_r.substring(2, 4);
        String r_byte_3 = string_r.substring(4, 6);
        String r_byte_4 = string_r.substring(6, 8);
        // Put bytes in least significant byte order.
        String r_bytes = r_byte_4 + r_byte_3 + r_byte_2 + r_byte_1;
        // Create payload from the hex bytes.
        String payload = "5403" + ptpMode + x_bytes + y_bytes + z_bytes + r_bytes;
        //System.out.println("Payload: " + payload);
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        //System.out.println("Command: " + start_bytes + len_hex + payload + checkSum_hex);
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetPTPLParams(double velocity, double acceleration)
    {
        // Convert device_name to hex String.
        String string_velocity = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(velocity))).toUpperCase());
        String string_acceleration = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(acceleration))).toUpperCase());

        // Add users request to payload.
        String payload = "550100" + string_velocity + string_acceleration;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetPTPLParams()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03550000AB");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return xyzVelocity, rVelocity, xyzAcceleration and rAcceleration.
        return values;
    }

    public void SetPTPWithLCmd(int ptpMode, double x, double y, double z, double r, double i)
    {
        String string_ptpMode = Long.parseLong(ptpMode + "", 16) + "";
        if (string_ptpMode.length() < 2) string_ptpMode = "0" + string_ptpMode;
        String string_x = Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase();
        String string_r = Integer.toHexString(Float.floatToIntBits(new Float(r))).toUpperCase();
        String string_i = Integer.toHexString(Float.floatToIntBits(new Float(i))).toUpperCase();
        // Add users request to payload.
        String payload = "560100" + string_ptpMode + string_x + string_y + string_z + string_r + string_i;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public double[] GetPTPJump2Params()
    {
        double values[] = {0, 0};
        // Connect with DOBOT and send payload.
        String reply = connect_Dobot("AAAA03570000A9");
        try
        {
            // Get payload from reply with using 'len' as payload size.
            reply = reply.substring(10, 6+(Integer.parseInt(Long.parseLong(reply.substring(4, 6), 16)+"") * 2));
            // Get float value from reply.
            System.out.println(reply);
            values[0] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(0, 8)), 16));
            values[1] = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(reply.substring(8, 16)), 16));
        }
        catch (Exception e)
        {
            System.out.println(error_cant_parse);
        }
        // Return startjumpHeight and endJumpHeight.
        return values;
    }

    public void SetPTPJump2Params(double startJumpHeight, double endJumpHeight)
    {
        // Convert device_name to hex String.
        String string_startJumpHeight = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(startJumpHeight))).toUpperCase());
        String string_endJumpHeight = hex_lsb(Integer.toHexString(Float.floatToIntBits(new Float(endJumpHeight))).toUpperCase());

        // Add users request to payload.
        String payload = "570100" + string_startJumpHeight + string_endJumpHeight;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetPTPPOCmd(int ptpMode, double x, double y, double z, double r)
    {
        String string_ptpMode = Long.parseLong(ptpMode + "", 16) + "";
        if (string_ptpMode.length() < 2) string_ptpMode = "0" + string_ptpMode;
        String string_x = Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase();
        String string_r = Integer.toHexString(Float.floatToIntBits(new Float(r))).toUpperCase();
        // Add users request to payload.
        String payload = "580100" + string_ptpMode + string_x + string_y + string_z + string_r;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    public void SetPTPPOWithLCmd(int ptpMode, double x, double y, double z, double r, double i)
    {
        String string_ptpMode = Long.parseLong(ptpMode + "", 16) + "";
        if (string_ptpMode.length() < 2) string_ptpMode = "0" + string_ptpMode;
        String string_x = Integer.toHexString(Float.floatToIntBits(new Float(x))).toUpperCase();
        String string_y = Integer.toHexString(Float.floatToIntBits(new Float(y))).toUpperCase();
        String string_z = Integer.toHexString(Float.floatToIntBits(new Float(z))).toUpperCase();
        String string_r = Integer.toHexString(Float.floatToIntBits(new Float(r))).toUpperCase();
        String string_i = Integer.toHexString(Float.floatToIntBits(new Float(i))).toUpperCase();
        // Add users request to payload.
        String payload = "560100" + string_ptpMode + string_x + string_y + string_z + string_r + string_i;
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    private void  _example()
    {
        String payload = "580000";
        // Get len_hex byte from payload.
        String len_hex = get_payload_len(payload);
        // Get checkSum_hex from payload.
        String checkSum_hex = get_checksum(payload);
        System.out.println(start_bytes + len_hex + payload + checkSum_hex);
        // Connect with DOBOT, send payload and retrieve reply from DOBOT.
        //connect_Dobot(start_bytes + len_hex + payload + checkSum_hex);
    }

    /**
     * Sockets and data conversions.
     */
    private DatagramSocket udp_socket = null;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";

    private String connect_Dobot(String payload)
    {
        String received = "";
        if (serialPort == null) {
            try {
                if (debug_mode) System.out.println(debug_mode_tag + "\u001B[32m[UDP connection|\u001B[34m" + dobot_hostname + ":" + dobot_port +"\u001B[32m]\u001B[0m" + "OutputSteam: " + payload);
                // Create a new UDP udp_socket.
                udp_socket = new DatagramSocket(dobot_port);
                // Set timeout.
                udp_socket.setSoTimeout(dobot_time_out);
                // Decode hex string to hex.
                byte[] buf = (HexBin.decode(payload));
                // Set address with dobot_hostname.
                InetAddress address = InetAddress.getByName(dobot_hostname);
                // Create DatagramPacket.
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, dobot_port);
                // Send packet to DOBOT.
                udp_socket.send(packet);
                // Wait some time.
                Thread.sleep(after_send_sleep);
                // Create a buffer for receiving data.
                byte[] buffer = new byte[1024];
                // Create a packet to receive data into the buffer
                DatagramPacket new_packet = new DatagramPacket(buffer, buffer.length);
                // Receive data from udp_socket.
                udp_socket.receive(new_packet);
                // If received "41542b5" wait for another packet this data is redundant.
                if (byteToHex(buffer).replace("ff", "00").contains("41542b5")) udp_socket.receive(new_packet);
                // Replace FF in hex to 00 in Hex.
                received = byteToHex(buffer).replace("ff", "00");
                if (debug_mode) System.out.println(debug_mode_tag + "\u001B[32m[UDP connection|\u001B[34m" + dobot_hostname + ":" + dobot_port +"\u001B[32m]\u001B[0m" + "InputStream: " + received);
                // Close udp_socket.
                udp_socket.close();
            } catch (Exception ex) {
                System.out.println("\u001B[31m[Communication error]\u001B[0m" + "\u001B[32m[UDP connection|\u001B[34m" + dobot_hostname + ":" + dobot_port +"\u001B[32m]\u001B[0m " + ex);
                udp_socket.close();
                after_send_sleep = 20;
            }
        }
        else
        {
            try
            {
                if (debug_mode) System.out.println(debug_mode_tag + "\u001B[32m[Serial connection|\u001B[34m" + this.getDobot_serial_port() + "\u001B[32m]\u001B[0m" + "OutputSteam: " + payload);
                // Decode hex string to hex and send it to serialPort.
                serialPort.writeBytes((HexBin.decode(payload)));
                // Sleep for 10 milliseconds, makes sure that the DOBOT has received the payload.
                Thread.sleep(after_send_sleep);
                // Receive hex string with received bytes length.
                received = serialPort.readHexString(serialPort.getInputBufferBytesCount(), dobot_time_out);
                // remove ' ' from received.
                received = received.replace(" ", "");
                if (debug_mode) System.out.println(debug_mode_tag + "\u001B[32m[Serial connection|\u001B[34m" + this.getDobot_serial_port() + "\u001B[32m]\u001B[0m" + "InputStream: " + received);
            }
            catch (Exception ex)
            {
                System.out.println("\u001B[31m[Communication error]\u001B[0m" + "\u001B[32m[Serial connection|\u001B[34m" + this.getDobot_serial_port() + "\u001B[32m]\u001B[0m " +  ex);
                after_send_sleep = 20;
            }
        }
        return received;
    }

    public boolean connect_serial()
    {
        boolean is_connected = true;
        String portName = "";
        try
        {
            // Search for used serial ports.
            String[] ports = SerialPortList.getPortNames();
            for(int i = 0; i < ports.length; i++)
            {
                // Use last found port.
                portName = ports[i];
            }
            // Bind portName to serialPort.
            serialPort = new SerialPort(portName);
            // Open port.
            serialPort.openPort();
            // Set parameters.
            serialPort.setParams (
                    SerialPort.BAUDRATE_115200,
                    jssc.SerialPort.DATABITS_8,
                    jssc.SerialPort.STOPBITS_1,
                    jssc.SerialPort.PARITY_NONE
            );
            this.setDobot_serial_port(portName);
        }
        catch (Exception ex)
        {
            System.out.println("\u001B[31m[Communication error]\u001B[0m" + "\u001B[32m[Serial connection|\u001B[34m" + this.getDobot_serial_port() + "\u001B[32m]\u001B[0m " +  ex);
            is_connected = false;
        }
        return is_connected;
    }

    public boolean connect_serial(String portName)
    {
        boolean is_connected = true;
        try
        {
            // Bind portName to serialPort.
            serialPort = new SerialPort(portName);
            // Open port.
            serialPort.openPort();
            // Set parameters.
            serialPort.setParams (
                    SerialPort.BAUDRATE_115200,
                    jssc.SerialPort.DATABITS_8,
                    jssc.SerialPort.STOPBITS_1,
                    jssc.SerialPort.PARITY_NONE
            );
            this.setDobot_serial_port(portName);
        }
        catch (Exception ex)
        {
            System.out.println("\u001B[31m[Communication error]\u001B[0m" + "\u001B[32m[Serial connection|\u001B[34m" + this.getDobot_serial_port() + "\u001B[32m]\u001B[0m " +  ex);
            is_connected = false;
        }
        return is_connected;
    }

    public boolean connect_udp(String hostname, int port, int time_out)
    {
        boolean is_connected = true;
        dobot_hostname = hostname;
        dobot_port = port;
        dobot_time_out = time_out;
        try
        {
            // Bind dobot_port to udp_socket.
            udp_socket = new DatagramSocket(dobot_port);
            // Check if udp_socket is connected to Host.
            is_connected = udp_socket.isConnected();
            // Close connection with Host.
            udp_socket.close();
        }
        catch (Exception e)
        {
            System.out.println("ERROR: Cant connect to DOBOT!");
            is_connected = false;
        }
        return is_connected;
    }

    public boolean connect_udp(String hostname, int time_out_inMilliseconds)
    {
        boolean is_connected = true;
        dobot_hostname = hostname;
        dobot_time_out = time_out_inMilliseconds;
        try
        {
            // Bind dobot_port to udp_socket.
            udp_socket = new DatagramSocket(dobot_port);
            // Check if udp_socket is connected to Host.
            is_connected = udp_socket.isConnected();
            // Close connection with Host.
            udp_socket.close();
        } catch (Exception e)
        {
            System.out.println("ERROR: Cant connect to DOBOT!");
            is_connected = false;
        }
        return is_connected;
    }

    private String byteToHex(final byte[] hash)
    {
        // Get formatter class.
        Formatter formatter = new Formatter();
        // Loop through hash.
        for (byte b : hash)
        {
            // Convert Byte to hex.
            formatter.format("%02x", b);
        }
        // Convert formatter to a String.
        String result = formatter.toString();
        // Close formatter.
        formatter.close();
        return result;
    }

    private String get_checksum(String payload)
    {
        int sum_length = 0;
        int index = 0;
        // Calculate checksum.
        while (index < payload.length())
        {
            // Convert Hex to decimal.
            String string_hex_to_decimal = Long.parseLong(payload.substring(index, 2 + index), 16) + "";
            // Convert string_hex_to_decimal to int.
            int int_decimal_value = Integer.parseInt(string_hex_to_decimal);
            // Add int_decimal_value to sum_length.
            sum_length = sum_length + int_decimal_value;
            // Add 2 to index.
            index = index + 2;
        }
        //System.out.println("Length: " + sum_length);
        // Convert decimal to bytes.
        //System.out.println("Bytes: " + Integer.toBinaryString(sum_length));
        String int_to_bytes = Integer.toBinaryString(sum_length) + "";
        // Get 8 LSB from sum_length.
        int LSB_bytes = 0;
        if (int_to_bytes.length() > 8) LSB_bytes = Integer.parseInt(int_to_bytes.substring(int_to_bytes.length() - 8));
        else LSB_bytes = Integer.parseInt(int_to_bytes);

        //System.out.println("8 LSB: " + LSB_bytes);
        // Convert LSB_bytes to int.
        byte LSB_decimal = (byte)(int)Integer.valueOf(LSB_bytes+"", 2);
        //System.out.println(LSB_decimal);
        String checkSum_hex = Integer.toHexString((256 - LSB_decimal)).toUpperCase() + "";
        // Calculate checkSum_decimal.
        String checkSum_decimal = (256 - LSB_decimal) + "";
        //System.out.println(checkSum_decimal.replace("-", ""));
        //System.out.println("CheckSum decimal: " + (256 - LSB_decimal) + "");
        //System.out.println("CheckSum: " + checkSum_hex);

        // When LSB_decimal is smaller then '0' make LSB_decimal positive.
        if (LSB_decimal < 0)
        {
            //System.out.println((Math.abs(LSB_decimal)));
            int LSB_decimal_positive = Math.abs(LSB_decimal);
            checkSum_hex = Integer.toHexString(LSB_decimal_positive).toUpperCase();
            // If hex value is not 2 in length add a 0.
            if (checkSum_hex.length() == 1) checkSum_hex = "0" + checkSum_hex;
            //System.out.println(checkSum_hex);
        }
        // Return checkSum_hex.
        return checkSum_hex;
    }

    private String get_payload_len(String payload)
    {
        int index = 0;
        int len = 0;
        // Calculate checksum.
        while (index < payload.length())
        {
            // Add 2 to index.
            index = index + 2;
            // Increase payload length with 1.
            len = len + 1;
        }
        // Convert integer to hex String.
        String len_hex = Integer.toHexString((len)).toUpperCase() + "";
        // If hex value is not 2 in length add a 0.
        if (len_hex.length() == 1) len_hex = "0" + len_hex;
        // Return len_hex.
        return len_hex;
    }

    private double[] get_coordinates_from_payload(String payload)
    {
        double[] array_coordinates = null;
        try {
            // Get x, y, z and r value from reply.
            String values = payload.substring(10);
            // First LSB on hex byte second convert hex value to long and lastly convert int bits to float.
            double x_bytes = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(values.substring(0, 8)), 16));
            double y_bytes = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(values.substring(8, 16)), 16));
            double z_bytes = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(values.substring(16, 24)), 16));
            double r_bytes = Float.intBitsToFloat((int) Long.parseLong(hex_lsb(values.substring(24, 32)), 16));
            // Create array_coordinates.
            double[] array_coordinates_comp = {x_bytes, y_bytes, z_bytes, r_bytes};
            array_coordinates = array_coordinates_comp;
        }
        catch (Exception ex) {}
        return array_coordinates;
    }

    public static byte[] decodeHexStringToHex(String hex)
    {
        // Create an array of the hex String.
        String[] list=hex.split("(?<=\\G.{2})");
        // Get bytes from String[].
        ByteBuffer buffer= ByteBuffer.allocate(list.length);
        for(String str: list)
            // Convert buffer to Hex.
            buffer.put((byte)Integer.parseInt(str,16));
        // Return buffer array.
        return buffer.array();

    }

    private String hex_lsb(String hex_bytes)
    {
        String lsb_hex_string = "";
        // Turn around hex_bytes with the LSB-method.
        for (int i = 0; i < hex_bytes.length()/2; i++)
        {
            lsb_hex_string = hex_bytes.substring(0 + (i * 2), 2 + (i * 2)) + lsb_hex_string;
        }
        // Make sure lsb_hex_string is 4 Bytes in Length a.k.a 8 in String length.
        if (lsb_hex_string.length()<8)
        {
            while (lsb_hex_string.length()<8)
            {
                lsb_hex_string = "0" + lsb_hex_string;
            }
        }
        // Return lsb_hex_string.
        return lsb_hex_string;
    }

    public boolean isDebug_mode() {
        return debug_mode;
    }

    public void setDebug_mode(boolean debug_mode) {
        this.debug_mode = debug_mode;
    }

    public String getDobot_serial_port() {
        return dobot_serial_port;
    }

    public void setDobot_serial_port(String dobot_serial_port) {
        this.dobot_serial_port = dobot_serial_port;
    }
}
