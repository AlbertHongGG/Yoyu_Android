package com.jasonhong.yoyu.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain-level Single Source of Truth for iPass card face IDs and CDN routing:
 * - Guarantees that card face ID 11 is the single, canonical default across the system.
 * - Enforces official white-listed ID validation (O(1) lookup).
 * - Transparently normalizes missing, corrupted, or unlisted IDs (such as legacy 1367) to ID 11.
 */
public final class CardFaceCatalog {

    public static final int DEFAULT_FACE_ID = 11;
    public static final String CDN_BASE_URL = "https://static01-ipass.cdn.hinet.net/ipassapp/cardface/";

    private static final int[] RAW_ALLOWED_IDS = new int[]{
            11, 1011, 1013, 4499, 4500, 4501, 4555, 4576, 4586, 4591, 4592, 4593, 4610, 4611, 4612, 4684, 4685, 4686, 4690, 4741, 4742, 4743, 4766, 4767, 4777, 4778, 4779, 4780, 4799, 4800, 4801, 4806, 4807, 4808, 4816, 4817, 4818, 4819, 4820, 4821, 4827, 4840, 4841, 4842, 4843, 4872, 4873, 4874, 4875, 4876, 4877, 4878, 4879, 4880, 4881, 4882, 4907, 4908, 4912, 4913, 4914, 4934, 4935, 4936, 4941, 4942, 4943, 4944, 4945, 4952, 4953, 4954, 4955, 4956, 4957, 4958, 4959, 4960, 4961, 4962, 4963, 4965, 4966, 4967, 4968, 4972, 4973, 4974, 4977, 4978, 4979, 4980, 4982, 4983, 4984, 4991, 4992, 4993, 4994, 4995, 4996, 4998, 4999, 5000, 5009, 5010, 5011, 5012, 5013, 5016, 5017, 5018, 5019, 5020, 5032, 5033, 5034, 5035, 5036, 5038, 5039, 5040, 5041, 5042, 5045, 5046, 5047, 5048, 5051, 5052, 5053, 5054, 5055, 5064, 5065, 5066, 5067, 5085, 5086, 5092, 5093, 5102, 5103, 5104, 5105, 5111, 5112, 5141, 5142, 5148, 5149, 5150, 5151, 5152, 5153, 5157, 5158, 5159, 5160, 5161, 5162, 5181, 5182, 5183, 5184, 5187, 5188, 5189, 5190, 5191, 5192, 5193, 5194, 5195, 5196, 5197, 5198, 5199, 5200, 5201, 5205, 5212, 5213, 5214, 5215, 5216, 5217, 5218, 5219, 5220, 5221, 5222, 5223, 5224, 5225, 5226, 5227, 5228, 5229, 5230, 5249, 5250, 5274, 5275, 5279, 5280, 5281, 5284, 5285, 5286, 5287, 5288, 5293, 5294, 5295, 5296, 5303, 5304, 5305, 5306, 5307, 5360, 5361, 5362, 5363, 5364, 5365, 5366, 5367, 5379, 5380, 5381, 5382, 5383, 5384, 5385, 5386, 5387, 5388, 5389, 5390, 5391, 5392, 5393, 5394, 5395, 5396, 5397, 5398, 5399, 5400, 5401, 5404, 5405, 5406, 5407, 5419, 5426, 5427, 5428, 5429, 5430, 5431, 5432, 5437, 5438, 5446, 5447, 5448, 5449, 5450, 5451, 5452, 5453, 5454, 5455, 5456, 5457, 5458, 5459, 5460, 5461, 5462, 5463, 5464, 5465, 5466, 5468, 5469, 5470, 5471, 5472, 5473, 5483, 5484, 5485, 5486, 5487, 5488, 5489, 5490, 5495, 5496, 5497, 5504, 5505, 5508, 5509, 5510, 5511, 5517, 5518, 5519, 5520, 5532, 5560, 5561, 5562, 5566, 5577, 5578, 5581, 5583, 5585, 5586, 5590, 5591, 5592, 5594, 5598, 5599, 5600, 5601, 5603, 5604, 5609, 5613, 5614, 5616, 5637, 5649, 5650, 5654, 5655, 5656, 5657, 5658, 5673, 5674, 5675, 5676, 5682, 5683, 5684, 5687, 5688, 5689, 5690, 5691, 5701, 5702, 5703, 5704, 5705, 5706, 5707, 5714, 5715, 5730, 5731, 5732, 5733, 5739, 5740, 5741, 5742, 5743, 5744, 5745, 5746, 5747, 5748, 5749, 5750, 5751, 5752, 5753, 5754, 5755, 5756, 5757, 5758, 5759, 5760, 5761, 5762, 5772, 5773, 5774, 5775, 5776, 5777, 5778, 5779, 5780, 5783, 5786, 5788, 5789, 5790, 5791, 5794, 5800, 5801, 5802, 5803, 5804, 5809, 5810, 5815, 5816, 5817, 5820, 5821, 5822, 5823, 5824, 5825, 5826, 5829, 5830, 5831, 5832, 5835, 5836, 5837, 5838, 5839, 5840, 5841, 5842, 5843, 5844, 5845, 5846, 5859, 5860, 5861, 5862, 5866, 5867, 5869, 5870, 5871, 5873, 5874, 5875, 5876, 5877, 5878, 5888, 5889, 5891, 5892, 5893, 5894, 5895, 5896, 5897, 5898, 5899, 5920, 5921, 5930, 5931, 5932, 5933, 5951, 5952, 5953, 5954, 5957, 5958, 5959, 5960, 5961, 5963, 5964, 5965, 5966, 5967, 5968, 5970, 5971, 5972, 5973, 5974, 5975, 5978, 5979, 5980, 5981, 5982, 5983, 5984, 5985, 5998, 5999, 6004, 6011, 6013, 6015, 6016, 6017, 6022, 6033, 6042, 6043, 6045, 6046, 6047, 6048, 6050, 6051, 6052, 6053, 6054, 6055, 6056, 6057, 6058, 6059, 6060, 6061, 6062, 6063, 6064, 6065, 6066, 6067, 6068, 6069, 6074, 6075, 6076, 6083, 6084, 6085, 6088, 6092, 6096, 6097, 6106, 6107, 6108, 6109, 6110, 6111, 6112, 6113, 6114, 6115, 6116, 6117, 6118, 6119, 6120, 6121, 6122, 6123, 6124, 6125, 6126, 6131, 6134, 6135, 6136, 6137, 6138, 6139, 6140, 6141, 6142, 6143, 6144, 6145, 6146, 6147, 6153, 6154, 6155, 6156, 6163, 6164, 6177, 6178, 6179, 6180, 6188, 6189, 6190, 6191, 6201, 6202, 6204, 6208, 6209, 6214, 6215, 6216, 6217, 6218, 6219, 6220, 6226, 6227, 6228, 6229, 6230, 6231, 6234, 6235, 6236, 6237, 6247, 6249, 6250, 6257, 6258, 6259, 6260, 6261, 6262, 6263, 6264, 6265, 6266, 6269, 6270, 6271, 6272, 6273, 6274, 6275, 6276, 6277, 6278, 6279, 6280, 6281, 6282, 6283, 6284, 6288, 6289, 6297, 6298, 6301, 6302, 6303, 6304, 6305, 6306, 6307, 6308, 6309, 6310, 6311, 6312, 6313, 6314, 6315, 6316, 6317, 6318, 6319, 6320, 6321, 6322, 6323, 6324, 6325, 6326, 6327, 6328, 6329, 6330, 6331, 6332, 6333, 6334, 6335, 6336, 6337, 6338, 6339, 6340, 6341, 6342, 6343, 6344, 6345, 6346, 6347, 6348, 6349, 6350, 6351, 6352, 6353, 6354, 6355, 6356, 6357, 6358, 6359, 6360, 6361, 6362, 6363, 6364, 6365, 6366, 6367, 6368, 6369, 6372, 6373, 6375, 6376, 6377, 6383, 6384, 6385, 6386, 6390, 6392, 6393, 6394, 6405, 6406, 6407, 6408, 6409, 6410, 6411, 6412, 6413, 6414, 6419, 6420, 6421, 6422, 6423, 6424, 6425, 6434, 6435, 6436, 6443, 6444, 6448, 6449, 6451, 6452, 6453, 6456, 6457, 6468, 6469, 6472, 6473, 6476, 6477, 6478, 6496, 6497, 6498, 6505, 6506, 6507, 6508, 6509, 6510, 6511, 6512, 6515, 6516, 6517, 6520, 6521, 6522, 6526, 6527, 6528, 6529, 6530, 6531, 6532, 6533, 6534, 6535, 6536, 6538, 6539, 6540, 6541, 6542, 6543, 6544, 6545, 6546, 6547, 6548, 6549, 6550, 6551, 6552, 6553, 6554, 6555, 6559, 6561, 6562, 6563, 6564, 6565, 6566, 6567, 6577, 6578, 6580, 6581, 6582, 6583, 6589, 6590, 6592, 6594, 6595, 6596, 6601, 6602, 6603, 6604, 6611, 6612, 6613, 6615, 6616, 6617, 6618, 6632, 6633, 6634, 6635, 6636, 6637, 6638, 6639, 6704, 6708, 6717, 6718, 6719, 6724, 6725, 6726, 6727, 6728, 6729, 6730, 6731, 6732, 6733, 6734, 6735, 6736, 6737, 6738, 6739, 6740, 6741, 6742, 6743, 6745, 6746, 6747, 6753, 6754, 6755, 6756, 6757, 6758, 6759, 6760, 6762, 6763, 6769, 6770, 6771, 6772, 6773, 6774, 6775, 6776, 6782, 6783, 6784, 6794, 6795, 6796, 6797, 6798, 6799, 6800, 6801, 6803, 6804, 6805, 6806, 6828, 6829, 6833, 6834, 6835, 6844, 6845, 6846, 6881, 6882, 6883, 6884
    };

    private static final Set<Integer> ALLOWED_SET;
    private static final List<Integer> ALLOWED_LIST;

    static {
        Set<Integer> set = new HashSet<>(RAW_ALLOWED_IDS.length * 2);
        List<Integer> list = new ArrayList<>(RAW_ALLOWED_IDS.length);
        for (int id : RAW_ALLOWED_IDS) {
            set.add(id);
            list.add(id);
        }
        ALLOWED_SET = Collections.unmodifiableSet(set);
        ALLOWED_LIST = Collections.unmodifiableList(list);
    }

    private CardFaceCatalog() {}

    /**
     * Checks if a card face ID is an officially supported image.
     */
    public static boolean isValid(int id) {
        return ALLOWED_SET.contains(id);
    }

    /**
     * Normalizes an ID: returns the ID itself if valid, or DEFAULT_FACE_ID (11) if invalid.
     */
    public static int normalize(int id) {
        return isValid(id) ? id : DEFAULT_FACE_ID;
    }

    /**
     * Resolves a remote cardFaceId string safely:
     * Returns the valid integer ID or DEFAULT_FACE_ID (11).
     */
    public static int resolveFromRemote(@Nullable String remoteFaceIdStr) {
        if (remoteFaceIdStr == null || remoteFaceIdStr.trim().isEmpty()) {
            return DEFAULT_FACE_ID;
        }
        try {
            int parsed = Integer.parseInt(remoteFaceIdStr.trim());
            return normalize(parsed);
        } catch (NumberFormatException e) {
            return DEFAULT_FACE_ID;
        }
    }

    /**
     * Resolves an ID from a full CDN URL or numeric string (for legacy migration or URL parsing).
     */
    public static int resolveFromUrl(@Nullable String url) {
        if (url == null || url.trim().isEmpty()) {
            return DEFAULT_FACE_ID;
        }
        try {
            String clean = url.trim();
            int lastSlash = clean.lastIndexOf('/');
            if (lastSlash >= 0) {
                clean = clean.substring(lastSlash + 1);
            }
            if (clean.endsWith(".webp")) {
                clean = clean.substring(0, clean.length() - 5);
            }
            int parsed = Integer.parseInt(clean);
            return normalize(parsed);
        } catch (Exception e) {
            return DEFAULT_FACE_ID;
        }
    }

    /**
     * Returns the canonical CDN image URL for a given card face ID.
     */
    @NonNull
    public static String getUrl(int faceId) {
        return CDN_BASE_URL + normalize(faceId) + ".webp";
    }

    /**
     * Returns the default card face CDN URL.
     */
    @NonNull
    public static String getDefaultUrl() {
        return getUrl(DEFAULT_FACE_ID);
    }

    /**
     * Returns the read-only ordered list of all allowed face IDs for the card face picker.
     */
    @NonNull
    public static List<Integer> getAllowedIds() {
        return ALLOWED_LIST;
    }
}
