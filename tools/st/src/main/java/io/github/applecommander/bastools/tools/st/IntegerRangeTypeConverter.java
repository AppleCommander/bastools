package io.github.applecommander.bastools.tools.st;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.ITypeConverter;

public class IntegerRangeTypeConverter implements ITypeConverter<List<Integer>> {
    @Override
    public List<Integer> convert(String value) {
        List<Integer> list = new ArrayList<>();
        String[] parts = value.split(",");
        for (String part : parts) {
            String[] range = part.split("-");
            if (range.length == 1) {
                list.add(Integer.parseInt(range[0]));
            } else if (range.length == 2) {
                int i0 = Integer.parseInt(range[0]);
                int i1 = Integer.parseInt(range[1]);
                for (int i = i0; i <= i1; i++) {
                    list.add(i);
                }
            } else {
                throw new RuntimeException("Expecting a single integer or two integers for a range");
            }
        }
        return list;
    }
}
