package tesi.smartwasting;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIot;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.DescribeThingRequest;
import com.amazonaws.services.iot.model.DescribeThingResult;
import com.amazonaws.services.iot.model.ListThingsRequest;
import com.amazonaws.services.iot.model.ListThingsResult;
import com.amazonaws.services.iot.model.ResourceNotFoundException;
import com.amazonaws.services.iot.model.ThingAttribute;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.amazonaws.metrics.AwsSdkMetrics.setRegion;

public class FindBin extends Thread {

    private Context context;
    private String category;
    private Location userLocation;
    private TextView resultView;

    public FindBin(Context context, String category, Location userLocation, TextView resultView) {
        this.context = context;
        this.category = category;
        this.userLocation = userLocation;
        this.resultView = resultView;
    }

    public void run() {
        AWSIot iotClient = this.getIotClient();
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
            ListThingsRequest listThingsRequest = new ListThingsRequest();
            listThingsRequest.setAttributeName("category");
            listThingsRequest.setAttributeValue(category.toUpperCase());
            ListThingsResult listThingsResult = iotClient.listThings(listThingsRequest);
            List<ThingAttribute> thingAttributes = listThingsResult.getThings();
            Map<String, String> attributes;
            double lat, lon;
            Location binLocation;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses;
            String addressLine, thingName = "", latitude = "", longitude = "", fillLevel = "";
            CreateThingShadows shadows = new CreateThingShadows();
            AWSIotDataClient awsIotDataClient = shadows.getAwsIotDataClient();
            GetThingShadowRequest getThingShadowRequest;
            GetThingShadowResult getThingShadowResult;
            HashMap<ThingAttribute, Double> fillLevels = new HashMap<>();
            SortMapByValues util = new SortMapByValues();
            Double fill_level = Double.valueOf(0);
            try {
                if(!thingAttributes.isEmpty()) {
                for (ThingAttribute t : thingAttributes) {
                    attributes = t.getAttributes();
                    if (attributes.get("latitude") != null && attributes.get("longitude") != null) {

                        lat = Double.parseDouble(attributes.get("latitude"));
                        lon = Double.parseDouble(attributes.get("longitude"));
                        binLocation = new Location("");
                        binLocation.setLatitude(lat);
                        binLocation.setLongitude(lon);

                        if (binLocation.distanceTo(userLocation) < 500) {

                            getThingShadowRequest = new GetThingShadowRequest().withThingName(t.getThingName());
                            getThingShadowResult = awsIotDataClient.getThingShadow(getThingShadowRequest);
                            String payload = new String(getThingShadowResult.getPayload().array());
                            JSONObject jsonObj = new JSONObject(payload);
                            fill_level = jsonObj.getJSONObject("state").getJSONObject("reported").getDouble("fill_level");
                            fillLevels.put(t, fill_level);
                        }
                    }
                }
                fillLevels = util.sortHashMapByValues(fillLevels);
                for (ThingAttribute k : fillLevels.keySet()) {
                    thingName = k.getThingName();
                    latitude = k.getAttributes().get("latitude");
                    longitude = k.getAttributes().get("longitude");
                    fillLevel = Double.toString(fillLevels.get(k));
                    break;
                }
                Log.d("PROVA", thingName + " " + latitude + " " + longitude + " " + fillLevel);
                if (latitude != null && longitude != null) {
                    lat = Double.parseDouble(latitude);
                    lon = Double.parseDouble(longitude);
                    addresses = geocoder.getFromLocation(lat, lon, 1);
                    addressLine = addresses.get(0).getAddressLine(0);
                    if (Double.parseDouble(fillLevel) > 80)
                        resultView.setText("Vai a buttarlo qui:\n" + thingName + "\n" + addressLine + "\n" + "Affrettati perchè è quasi pieno!");
                    else
                        resultView.setText("Vai a buttarlo qui:\n" + thingName + "\n" + addressLine + "\nPieno al " + fillLevel.substring(0, 5) + "%\n");
                }
                } else
                    resultView.append("Nessun cassonetto nelle vicinanze :-(");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch(ResourceNotFoundException e) {
                resultView.setText("Non ho trovato informazioni sullo stato dei cassonetti");
            } catch (Exception e) {
                e.printStackTrace();
            }

    }


    private AWSIot getIotClient() {
        BasicAWSCredentials cred = new BasicAWSCredentials("-",
                "-");
        AWSIot iotClient = new AWSIotClient(cred);
        iotClient.setRegion(Region.getRegion(Regions.EU_WEST_1));
        return iotClient;
    }


}
