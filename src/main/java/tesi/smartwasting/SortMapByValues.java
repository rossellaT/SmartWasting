package tesi.smartwasting;

import com.amazonaws.services.iot.model.ThingAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class SortMapByValues {
    public LinkedHashMap<ThingAttribute, Double> sortHashMapByValues(
            HashMap<ThingAttribute, Double> passedMap) {
        List<ThingAttribute> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);

        LinkedHashMap<ThingAttribute, Double> sortedMap =
                new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<ThingAttribute> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                ThingAttribute key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
