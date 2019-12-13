package Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Table {
    private List<TableElement> symbolTable = new ArrayList<>();

    public TableElement get(int index) {
        return symbolTable.get(index);
    }

    public TableElement getElementAllLever(String name, int level) {
        while (level > -1) {
            for (TableElement element : symbolTable) {
                if (element.getName().equals(name)
                        && element.getLevel() == level) {
                    return element;
                }
            }
            level--;
        }
        return null;
    }

    public TableElement getElementCurrent(String name, int level) {
        for (TableElement element : symbolTable) {
            if (element.getName().equals(name) && element.getLevel() == level) {
                return element;
            }
        }
        return null;
    }

    public void add(TableElement element) {
        symbolTable.add(element);
    }

    public void add(int index, TableElement element) {
        symbolTable.add(index, element);
    }

    public void remove(int index) {
        symbolTable.remove(index);
    }

    public void removeAll() {
        symbolTable.clear();
    }

    /**
     * 更新符号表，去除不在其中的元素
     * @param level 作用域
     */
    public void update(int level) {
        for (int i = 0; i < symbolTable.size(); i++) {
            if (symbolTable.get(i).getLevel() > level) {
                remove(i);
            }
        }
    }

    public int size() {
        return symbolTable.size();
    }
}
