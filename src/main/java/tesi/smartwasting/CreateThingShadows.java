package tesi.smartwasting;

import android.os.StrictMode;
import android.util.Log;


import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.services.iotdata.model.PublishRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;

import java.nio.ByteBuffer;
import java.util.Random;

public class CreateThingShadows extends Thread {

    private AWSIotDataClient awsIotDataClient;
    private String akey, skey, shadowPayload, thingName;
    private Random rnd = new Random();

    public CreateThingShadows() {

        akey = "-";
        skey = "-";
        awsIotDataClient = new AWSIotDataClient(new BasicAWSCredentials(akey, skey));
        awsIotDataClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

    public AWSIotDataClient getAwsIotDataClient() {
        return this.awsIotDataClient;
    }

    public void run() {
        while (true) {
            getRandomPayloads();
            UpdateThingShadowRequest updateThingShadowRequest = new UpdateThingShadowRequest()
                    .withThingName(this.thingName)
                    .withPayload(ByteBuffer.wrap(this.shadowPayload.getBytes()));
            Log.d("START TIME",System.currentTimeMillis()+"");
            UpdateThingShadowResult getThingShadowResult = awsIotDataClient.updateThingShadow(updateThingShadowRequest);
            Log.d("thing",thingName);
            try {
                sleep(rnd.nextInt(1000) + 10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
//
    private void getRandomPayloads() {
        this.thingName = "Cassonetto" + (rnd.nextInt(25000) + 1);
        float fill_level = rnd.nextFloat() * 100;
        shadowPayload = "{\n" + "\"state\"" + " : " + "{\n" +
                "  \"reported\": {\n" +
                "    \"fill_level\": " + fill_level + "\n" +
                "  }\n" +
                "}\n}";

    }


}
