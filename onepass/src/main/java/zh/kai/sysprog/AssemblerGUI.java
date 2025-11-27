package zh.kai.sysprog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.Assembler;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Operation;

public class AssemblerGUI extends JFrame{

    public final int WIDTH = 1000;
    public final int HEIGHT = 800;

    Assembler asm;

    
    private JTextArea sourceCodeArea;
    private DefaultTableModel opcodeTableModel;
    private JTable opcodeTable;

    private DefaultTableModel symTabModel;
    private DefaultTableModel meetTabModel;
    private JTextArea objectCodeArea;
    private JTextArea errorsPassArea;

    public AssemblerGUI(){
        setTitle("AssemblerGUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2,10,0));

        mainPanel.add(createColumn1());
        mainPanel.add(createColumn2());

        init();

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Центрирование окна
        
    }

    private JPanel createColumn1(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Исходные данные"));

        sourceCodeArea = new JTextArea(20, 30);
        sourceCodeArea.setText(Main.CODE.trim()); // Заполнение примером
        Font font = new Font("Consolas", Font.PLAIN, 14);
        sourceCodeArea.setFont(font);

        sourceCodeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                init(); // Вызываем init() при вставке текста
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                init(); // Вызываем init() при удалении текста
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Изменение атрибутов: обычно игнорируется для простого текста
            }
        });

        JScrollPane sourceCodeScrollPane = new JScrollPane(sourceCodeArea);
        sourceCodeScrollPane.setPreferredSize(new Dimension(WIDTH/2, HEIGHT));
        sourceCodeScrollPane.setBorder(new TitledBorder("Исходный код"));
        panel.add(sourceCodeScrollPane);

        JPanel asmButtons = new JPanel(new FlowLayout());
        //JButton initButton = new JButton("Load Data");
        JButton stepButton = new JButton("Step");
        JButton fullButton = new JButton("Full");
        //initButton.addActionListener(new InitListener());
        stepButton.addActionListener(new StepListener());
        fullButton.addActionListener(new FullListener());

        //asmButtons.add(initButton);
        asmButtons.add(stepButton);
        asmButtons.add(fullButton);
        //asmButtons.setPreferredSize(new Dimension(100,HEIGHT/2-2));
        panel.add(asmButtons);


        String[] opcodeColumns = {"Имя", "Код", "Длина"};
        opcodeTableModel = new DefaultTableModel(opcodeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Разрешаем редактирование
            }
        };
        opcodeTable = new JTable(opcodeTableModel);
        parseOpcodeData(Main.OPER);
        JScrollPane opCodesScrolPane = new JScrollPane(opcodeTable);
        //opCodesScrolPane.setPreferredSize(new Dimension(WIDTH/2-20-120, HEIGHT/2-20));
        opCodesScrolPane.setBorder(new TitledBorder("Операции"));
        panel.add(opCodesScrolPane);

        JPanel opCodesButtons = new JPanel(new FlowLayout());
        JButton addRowButton = new JButton("+КО");
        JButton removeRowButton = new JButton("-КО");
        addRowButton.addActionListener(new AddRowListener());
        removeRowButton.addActionListener(new RemoveRowListener());
        opCodesButtons.add(addRowButton);
        opCodesButtons.add(removeRowButton);
        //opCodesButtons.setPreferredSize(new Dimension(100,HEIGHT/2-2));
        panel.add(opCodesButtons);

        return panel;
    }
    
    // Парсинг начальных данных Opcode (из Main.opcod)
    private void parseOpcodeData(String opcod) {
        asm = new Assembler("", "");
        List<Operation> ops = asm.parseOpCode(opcod);
        for (Operation op : ops) {
            opcodeTableModel.addRow(new Object[]{op.getOperationName(), String.valueOf(op.getCode()), String.valueOf(op.getLenght())});
        }
    }

    public void init(){
        String code = sourceCodeArea.getText().trim();
        StringBuilder opcodeData = new StringBuilder();
        for (int i = 0; i < opcodeTableModel.getRowCount(); i++) {
            String n = (String) opcodeTableModel.getValueAt(i, 0);
            String c = (String) opcodeTableModel.getValueAt(i, 1);
            String l = (String) opcodeTableModel.getValueAt(i, 2);
            opcodeData.append(n).append(" ").append(c).append(" ").append(l).append("\n");
        }
        asm = new Assembler(code, opcodeData.toString());
        asm.init();
        System.out.println("Data has been load");
        objectCodeArea.setText("");
        errorsPassArea.setText("");
        symTabModel.setRowCount(0);
        meetTabModel.setRowCount(0);
    }

    private class InitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            init();
        }
        
    }

    private class StepListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(asm!=null){
                System.out.println(asm.getCodeLines().get(asm.getLinePos()));
                if(asm.passStep()){
                    updateData();
                }else {
                    String err = "";
                    for(String s:asm.getErrors()){
                        err += s+"\n";
                    }
                    errorsPassArea.setText(err);
                }
            }
        }
        
    }

    private class FullListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(asm!=null){
                if(asm.passFull()){
                    updateData();
                }else {
                    String err = "";
                    for(String s:asm.getErrors()){
                        err += s+"\n";
                    }
                    errorsPassArea.setText(err);
                }
            }
        }
        
    }

    private void updateData(){
        String obj = asm.toBin();
        Map<String,Address> sym = asm.getSymTab();
        Map<CodeLine,String> meet = asm.getMetLabels();
        objectCodeArea.setText(obj);
        symTabModel.setRowCount(0);
        for(String l: sym.keySet()){
            symTabModel.addRow(new Object[]{l,sym.get(l)});
        }
        meetTabModel.setRowCount(0);
        for(CodeLine cl: meet.keySet()){
            meetTabModel.addRow(new Object[]{cl.getAddress(),meet.get(cl)});
        }
    }

    private class AddRowListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            opcodeTableModel.addRow(new Object[]{"<ИМЯ>", "<КОД>", "<ДЛИН>"});
        }
    }

    /**
     * Удаляет выбранную строку из таблицы Opcode.
     */
    private class RemoveRowListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = opcodeTable.getSelectedRow();
            if (selectedRow != -1) { // Проверяем, что строка выбрана
                opcodeTableModel.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(AssemblerGUI.this, 
                    "Выберите строку для удаления.", 
                    "Ошибка удаления", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JPanel createColumn2(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Результат"));

        objectCodeArea = new JTextArea();
        objectCodeArea.setEditable(false);
        objectCodeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane objCodeScrollPane = new JScrollPane(objectCodeArea);
        objCodeScrollPane.setBorder(new TitledBorder("Объектный код"));
        objCodeScrollPane.setPreferredSize(new Dimension(WIDTH/2,HEIGHT/2));
        panel.add(objCodeScrollPane);

        JPanel tabs = new JPanel(new GridLayout(1,2,10,0));
        String[] symTabColumns = {"Имя", "Адрес"};
        symTabModel = new DefaultTableModel(symTabColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable symTable = new JTable(symTabModel);
        JScrollPane symTabScrollPane = new JScrollPane(symTable);
        symTabScrollPane.setBorder(new TitledBorder("Таблица символических имен"));
        tabs.add(symTabScrollPane);

        String[] meetTabColumns = {"Адрес", "Имя"};
        meetTabModel = new DefaultTableModel(meetTabColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable meetTable = new JTable(meetTabModel);
        JScrollPane meetTabScrollPane = new JScrollPane(meetTable);
        meetTabScrollPane.setBorder(new TitledBorder("Таблица встреченных имен"));
        tabs.add(meetTabScrollPane);
        tabs.setPreferredSize(new Dimension(WIDTH/2,HEIGHT/4));
        panel.add(tabs);

        errorsPassArea = new JTextArea();
        errorsPassArea.setEditable(false);
        JScrollPane errorsScrollPane = new JScrollPane(errorsPassArea);
        errorsScrollPane.setBorder(new TitledBorder("Ошибки"));
        panel.add(errorsScrollPane);


        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AssemblerGUI().setVisible(true);
        });
    }

}
