package com.giga.nexas.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * PROJECT PATCH (not upstream): drop-in replacement for hutool BeanUtil.copyProperties(bheObj, bsdxObj) inside the
 * BHE->BSDX manual mappers. BeanUtil silently loses List<BheInfoCollection> (field names/classes differ:
 * bheInfoCollectionList* vs bsdxInfoCollectionList*) and nested polymorphic SkillInfoObject data. This does a JSON copy:
 * rename bheInfoCollection* keys, re-key nested BHE typeIds to the BSDX typeId of the same class, keep dst's typeId/slotNum.
 */
public final class ProjJsonCopy {
    private static final ObjectMapper OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static Map<Integer, Integer> TYPE;

    public static void copy(Object src, Object dst) {
        if (src == null || dst == null) return;
        try {
            JSONObject j = JSONUtil.parseObj(JSONUtil.toJsonStr(src).replace("\"bheInfoCollection", "\"bsdxInfoCollection"));
            j.remove("typeId"); j.remove("slotNum");
            remap(j, true);
            OM.readerForUpdating(dst).readValue(j.toString());
        } catch (Exception e) {
            throw new RuntimeException("ProjJsonCopy failed: " + src.getClass().getSimpleName() + " -> " + dst.getClass().getSimpleName(), e);
        }
    }

    private static void remap(Object node, boolean top) {
        if (TYPE == null) {
            TYPE = new HashMap<>();
            Map<String, Integer> bs = new HashMap<>();
            for (int t = 0x23; t >= 0; t--)
                bs.put(com.giga.nexas.dto.bsdx.waz.wazfactory.SkillInfoFactory.createCEventObjectByTypeBsdx(t).getClass().getSimpleName(), t);
            for (int t = 0; t <= 0x2B; t++) {
                String n = com.giga.nexas.dto.bhe.waz.wazfactory.SkillInfoFactory.createCEventObjectByTypeBhe(t).getClass().getSimpleName();
                if (bs.containsKey(n)) TYPE.put(t, bs.get(n));
            }
        }
        if (node instanceof JSONObject o) {
            if (!top && o.containsKey("typeId") && o.containsKey("startFrame")) {
                Integer t = o.getInt("typeId");
                if (t != null && TYPE.containsKey(t)) o.set("typeId", TYPE.get(t));
            }
            for (Object v : o.values()) remap(v, false);
        } else if (node instanceof JSONArray a) {
            for (Object v : a) remap(v, false);
        }
    }

    /** index in a BHE unit list of the unit whose getUnitSlotNum()==slot (BHE readers only store present units), or -1 */
    public static int slotIndex(java.util.List<?> units, int slot) {
        if (units == null) return -1;
        for (int k = 0; k < units.size(); k++) {
            try {
                Object v = units.get(k).getClass().getMethod("getUnitSlotNum").invoke(units.get(k));
                if (v != null && ((Integer) v) == slot) return k;
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        return -1;
    }
}
