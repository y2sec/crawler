package naverNews;

import java.util.ArrayList;

public class Clustering {
    public Clustering() {
        super();
    }
    public double jaccard(String str1, String str2) {
        ArrayList<String> union = new ArrayList<>();
        ArrayList<String> intersection = new ArrayList<>();

        ArrayList<String> set1 = new ArrayList<>();
        ArrayList<String> set2 = new ArrayList<>();

        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        for (int i = 0; i < str1.length() - 1; ++i) {
            String s = str1.substring(i, i + 2);
            if (s.matches("[0-9a-zA-Zㄱ-ㅎㅏ-ㅣ가-힝]*"))
                set1.add(s);
        }

        for (int i = 0; i < str2.length() - 1; ++i) {
            String s = str2.substring(i, i + 2);
            if (s.matches("[0-9a-zA-Zㄱ-ㅎㅏ-ㅣ가-힝]*"))
                set2.add(s);
        }

        for (String s : set1) {
            if (set2.remove(s))
                intersection.add(s);
            union.add(s);
        }
        union.addAll(set2);

        double jakard = 1;

        if (union.size() == 0)
            return jakard;
        else
            jakard = (double) intersection.size() / (double) union.size();
        return jakard;
    }
}
