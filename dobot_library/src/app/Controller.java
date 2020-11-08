package app;


public class Controller
{
    public static Dobot dobot;
    public static Dobot dobot1;

    public void initialize()
    {
        dobot = new Dobot();
        dobot.connect_serial();

        dobot.setDebug_mode(true);
        System.out.println("Debug mode: " + dobot.isDebug_mode());;

        Thread thread = new Thread(() ->
        {
            System.out.println(dobot.GetDeviceName());
            System.out.println(dobot.GetWifiGateway());
            System.out.println(dobot.GetWIFISSID());
            System.out.println(dobot.GetWIFIPassword());
            System.out.println(dobot.GetWIFINetmask());
            System.out.println(dobot.GetWIFIConnectStatus());

            double pose[]  = dobot.GetPose();
            for (int i = 0; i < pose.length; i++) System.out.println(pose[i]);

            try
            {
                while (true)
                {
                    dobot.SetPTPCmd(250, -200, 0, 0);
                    Thread.sleep(2000);
                    dobot.SetPTPCmd(250, 200, 0, 0);
                    Thread.sleep(2000);
                    dobot.SetPTPCmd(250, 0, 30, 0);
                    Thread.sleep(2000);
                }
            }
            catch (Exception ex) {}
        });
        thread.start();
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }
}
