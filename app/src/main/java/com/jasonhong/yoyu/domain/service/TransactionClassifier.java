package com.jasonhong.yoyu.domain.service;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 交易分類決策器 (Transaction Classifier)
 * 遵循單一職責原則 (SRP)，專注判定原始交易 DTO 屬於大眾運輸 (Transit) 還是零售/單筆 (Retail)。
 * 採用雙層守護機制 (Dual-Guard Strategy)：
 * 1. 已知大眾運輸業者代碼判定 (F: 台北/新北捷運, I: 高雄捷運/輕軌, 6: 台鐵, 2: 公車客運)
 * 2. 乘車行為語意本質特徵判定 (xtype 包含進出站/上下車，或 InquireIcon 包含大眾運輸圖示)
 */
public class TransactionClassifier {

    private static final Set<String> KNOWN_TRANSIT_DATA_SOURCES = new HashSet<>(Arrays.asList(
            "F", // 台北捷運 / 新北捷運 (Taipei / New Taipei MRT)
            "I", // 高雄捷運 / 高雄輕軌 (Kaohsiung MRT / LRT)
            "6", // 臺灣鐵路 (TRA)
            "2"  // 市區公車 / 客運 (Bus)
    ));

    /**
     * 判定單筆原始交易是否具備大眾運輸特徵
     */
    public boolean isTransit(RawTransactionDto raw) {
        if (raw == null) {
            return false;
        }

        String dataSource = raw.getDataSource();
        if (dataSource != null && KNOWN_TRANSIT_DATA_SOURCES.contains(dataSource.trim())) {
            return true;
        }

        // 語意本質防護：檢查 xtype
        String xtype = raw.getXtype();
        if (xtype != null) {
            if (xtype.contains("進站") || xtype.contains("出站") ||
                    xtype.contains("段次上車") || xtype.contains("段次下車")) {
                return true;
            }
        }

        // 圖示特徵防護：檢查 InquireIcon
        String icon = raw.getInquireIcon();
        if (icon != null) {
            if (icon.contains("mrt") || icon.contains("train") || icon.contains("bus")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判定整組交易 (按 DataSource 分組後) 是否應作為大眾運輸處理
     */
    public boolean isTransitGroup(String dataSource, List<RawTransactionDto> group) {
        if (dataSource != null && KNOWN_TRANSIT_DATA_SOURCES.contains(dataSource.trim())) {
            return true;
        }

        if (group != null && !group.isEmpty()) {
            for (RawTransactionDto raw : group) {
                if (isTransit(raw)) {
                    return true;
                }
            }
        }

        return false;
    }
}
