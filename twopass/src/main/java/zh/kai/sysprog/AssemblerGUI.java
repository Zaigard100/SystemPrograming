package zh.kai.sysprog;

import zh.kai.sysprog.Assembler.AdderssationType;
import zh.kai.sysprog.asm.Address;
import zh.kai.sysprog.asm.CodeLine;
import zh.kai.sysprog.asm.Errors;
import zh.kai.sysprog.asm.Segment;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Map;


public class AssemblerGUI extends JFrame {

    AdderssationType currentType;
    Assembler asm;

    // --- Колонка 1: Исходные данные ---
    private JComboBox<AdderssationType> type;
    private JTextArea sourceCodeArea;
    private DefaultTableModel opcodeTableModel;
    private JTable opcodeTable;

    // --- Колонка 2: Первый проход ---
    private DefaultTableModel auxTableModel;
    private DefaultTableModel symTabModel;
    private JTextArea errorsPass1Area;

    // --- Колонка 3: Второй проход ---
    private DefaultTableModel relTabModel;
    private DefaultTableModel extTabModel;
    private JTextArea objectCodeArea;
    private JTextArea errorsPass2Area;

    public AssemblerGUI() {
        setTitle("AssemblerGUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1400, 800));

        // Основная панель с тремя колонками
        JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 0)); // 1 строка, 3 столбца, 10px горизонтальный отступ
        
        mainPanel.add(createColumn1());
        mainPanel.add(createColumn2());
        mainPanel.add(createColumn3());

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Центрирование окна
    }

    // Создание первой колонки: Исходные данные
    private JPanel createColumn1() {
        JPanel up_panel = new JPanel();
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("1. Исходные данные (Редактируемые)"));

        // 1.1. Исходный код (JTextArea)
        sourceCodeArea = new JTextArea(20, 30);
        sourceCodeArea.setText(Main.text.trim()); // Заполнение примером
        Font font = new Font("Consolas", Font.PLAIN, 14);
        sourceCodeArea.setFont(font);
        JScrollPane sourceCodeScrollPane = new JScrollPane(sourceCodeArea);
        sourceCodeScrollPane.setPreferredSize(new Dimension(450, 450));
        sourceCodeScrollPane.setBorder(new TitledBorder("Исходный код"));
        panel.add(sourceCodeScrollPane, BorderLayout.NORTH);

        // 1.2. Таблица кодов opcode (JTable)
        String[] opcodeColumns = {"Имя", "Код", "Длина"};
        opcodeTableModel = new DefaultTableModel(opcodeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Разрешаем редактирование
            }
        };
        opcodeTable = new JTable(opcodeTableModel);
        
        // Заполнение примером из Main.opcod
        parseOpcodeData(Main.opcod);

        JButton addRowButton = new JButton("+КО");
        JButton removeRowButton = new JButton("-КО");
        addRowButton.addActionListener(new AddRowListener());
        removeRowButton.addActionListener(new RemoveRowListener());

        JScrollPane opcodeScrollPane = new JScrollPane(opcodeTable);
        opcodeScrollPane.setBorder(new TitledBorder("Таблица кодов операций (Opcode)"));
        opcodeScrollPane.setPreferredSize(new Dimension(450, 200));
        panel.add(opcodeScrollPane, BorderLayout.CENTER);
        
        // 1.3. Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton pass1Button = new JButton("Первый проход");
        JButton pass2Button = new JButton("Второй проход");
        
        pass1Button.addActionListener(new Pass1ButtonListener());
        pass2Button.addActionListener(new Pass2ButtonListener());

        buttonPanel.add(addRowButton);
        buttonPanel.add(removeRowButton);
        buttonPanel.add(pass1Button);
        buttonPanel.add(pass2Button);
        
        //panel.add(buttonPanel, BorderLayout.SOUTH);

        DefaultComboBoxModel<AdderssationType> typeBoxModel = new DefaultComboBoxModel<>();
        typeBoxModel.addElement(AdderssationType.CHAINED);
        typeBoxModel.addElement(AdderssationType.DIRECT);
        typeBoxModel.addElement(AdderssationType.RELATIVE);
        type = new JComboBox<>(typeBoxModel);

        up_panel.add(type,BorderLayout.NORTH);
        up_panel.add(panel,BorderLayout.CENTER);
        up_panel.add(buttonPanel,BorderLayout.SOUTH);
        return up_panel;
    }

    // Создание второй колонки: Первый проход
    private JPanel createColumn2() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 10));
        panel.setBorder(new TitledBorder("2. Первый проход"));

        // 2.1. Вспомогательная таблица (JTable)
        String[] auxColumns = {"Адрес (hex)", "Текст"};
        auxTableModel = new DefaultTableModel(auxColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable auxTable = new JTable(auxTableModel);
        JScrollPane auxScrollPane = new JScrollPane(auxTable);
        auxScrollPane.setBorder(new TitledBorder("Вспомогательная таблица"));
        panel.add(auxScrollPane);

        // 2.2. Таблица символических имен (JTable)
        String[] symTabColumns = {"Имя", "Адрес","Внеш.","Располож."};
        symTabModel = new DefaultTableModel(symTabColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable symTable = new JTable(symTabModel);
        JScrollPane symTabScrollPane = new JScrollPane(symTable);
        symTabScrollPane.setBorder(new TitledBorder("Таблица символических имен"));
        panel.add(symTabScrollPane);

        // 2.3. Ошибки первого прохода (JTextArea)
        errorsPass1Area = new JTextArea();
        errorsPass1Area.setEditable(false);
        JScrollPane errors1ScrollPane = new JScrollPane(errorsPass1Area);
        errors1ScrollPane.setBorder(new TitledBorder("Ошибки первого прохода"));
        panel.add(errors1ScrollPane);

        return panel;
    }

    // Создание третьей колонки: Второй проход
    private JPanel createColumn3() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 10));
        panel.setBorder(new TitledBorder("3. Второй проход"));

        // 2.2. Таблица символических имен (JTable)
        String[] relTabColumns = {"Адрес","Внеш. имя","Располож."};
        relTabModel = new DefaultTableModel(relTabColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable relTable = new JTable(relTabModel);
        JScrollPane relTabScrollPane = new JScrollPane(relTable);
       relTabScrollPane.setBorder(new TitledBorder("Таблица перемещений"));

        String[] extTabColumns = {"Внеш. имя","Адрес","Располож."};
        extTabModel = new DefaultTableModel(extTabColumns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Неизменяемая
            }
        };
        JTable extTable = new JTable(extTabModel);
        JScrollPane extTabScrollPane = new JScrollPane(extTable);
        extTabScrollPane.setBorder(new TitledBorder("Внешние имена"));

        JPanel relAndExtData = new JPanel(new GridLayout(1, 2, 0, 10));

        relAndExtData.add(relTabScrollPane);
        relAndExtData.add(extTabScrollPane);

        panel.add(relAndExtData);

        // 3.1. Объектный код (JTextArea)
        objectCodeArea = new JTextArea();
        objectCodeArea.setEditable(false);
        objectCodeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane objCodeScrollPane = new JScrollPane(objectCodeArea);
        objCodeScrollPane.setBorder(new TitledBorder("Объектный код"));

        panel.add(objCodeScrollPane);

        // 3.2. Ошибки второго прохода (JTextArea)
        errorsPass2Area = new JTextArea();
        errorsPass2Area.setEditable(false);
        JScrollPane errors2ScrollPane = new JScrollPane(errorsPass2Area);
        errors2ScrollPane.setBorder(new TitledBorder("Ошибки второго прохода"));
        panel.add(errors2ScrollPane);

        return panel;
    }

    // Парсинг начальных данных Opcode (из Main.opcod)
    private void parseOpcodeData(String opcod) {
        String[] lines = opcod.trim().split("\n");
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 3) {
                opcodeTableModel.addRow(new Object[]{parts[0], parts[1], parts[2]});
            }
        }
    }

    // --- Listeners для кнопок управления таблицей Opcode ---

    /**
     * Добавляет новую пустую строку в таблицу Opcode.
     * Новая строка будет иметь значения: {"<ИМЯ>", "00", "1"}
     */
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

    // --- Listeners для кнопок ---

    // Кнопка "Первый проход"
    private class Pass1ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 1. Сбор данных из GUI
            String sourceCode = sourceCodeArea.getText();
            StringBuilder opcodeData = new StringBuilder();
            for (int i = 0; i < opcodeTableModel.getRowCount(); i++) {
                // Предполагаем, что данные корректно вводятся пользователем
                String name = (String) opcodeTableModel.getValueAt(i, 0);
                String code = (String) opcodeTableModel.getValueAt(i, 1);
                String length = (String) opcodeTableModel.getValueAt(i, 2);
                opcodeData.append(name).append(" ").append(code).append(" ").append(length).append("\n");
            }

            // 2. Очистка предыдущих результатов
            auxTableModel.setRowCount(0);
            symTabModel.setRowCount(0);
            relTabModel.setRowCount(0);
            extTabModel.setRowCount(0);
            errorsPass1Area.setText("");
            errorsPass2Area.setText("");
            objectCodeArea.setText("");

            // --- Подключение к логике (Необходимо модифицировать Assembler.java) ---
            try {
                currentType = (AdderssationType) type.getSelectedItem();
                asm = createAssemblerInstance(sourceCode, opcodeData.toString());
                asm.init();
                if (asm.firstPass()) {
                    // Успех - заполняем таблицы
                    
                    // Вспомогательная таблица
                    for (CodeLine cl : asm.getCodeLines()) {
                        String[] split = cl.toAdditionString().split("\\s+", 2);

                        auxTableModel.addRow(new Object[]{
                            split[0],
                            split[1]
                        });
                    }
                    for (Segment s : asm.getSegments()) {
                        for (CodeLine cl : s.getCodeLines()) {
                            String[] split = cl.toAdditionString().split("\\s+", 2);

                            auxTableModel.addRow(new Object[]{
                                split[0],
                                split[1]
                            });
                        }
                    }

                    asm.getSymTab().entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(
                        Comparator.comparing(Address::getAddress)
                    ))
                    .forEach(entry -> {
                        symTabModel.addRow(new Object[]{
                            entry.getKey(),
                            String.format("%06X", entry.getValue().getAddress()),
                            asm.getExternalLinks().keySet().contains(entry.getKey())?1:0,
                            asm.getCodeLines().getFirst().getLabel()
                        });
                    });
                    for (Segment s : asm.getSegments()) {
                        s.getSymTab().entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(
                            Comparator.comparing(Address::getAddress)
                        ))
                        .forEach(entry -> {
                            symTabModel.addRow(new Object[]{
                                entry.getKey(),
                                String.format("%06X", entry.getValue().getAddress()),
                                s.getExternalLinks().keySet().contains(entry.getKey())?1:0,
                                s.getName()
                            });
                        });
                    }
                    
                    //JOptionPane.showMessageDialog(AssemblerGUI.this, "Первый проход завершен успешно.", "Успех", JOptionPane.INFORMATION_MESSAGE);

                } else {
                    // Ошибка - заполняем область ошибок
                    errorsPass1Area.setText(Errors.getPart1());
                    JOptionPane.showMessageDialog(AssemblerGUI.this, "Обнаружены ошибки в первом проходе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }

            } catch (HeadlessException ex) {
                errorsPass1Area.setText("Критическая ошибка: " + ex.getMessage());
            }
            // ----------------------------------------------------------------------
        }
    }
    
    // Кнопка "Второй проход"
    private class Pass2ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
             // 1. Очистка предыдущих результатов второго прохода
            relTabModel.setRowCount(0);
            extTabModel.setRowCount(0);
            errorsPass2Area.setText("");
            objectCodeArea.setText("");
            
            StringBuilder opcodeData = new StringBuilder();
            for (int i = 0; i < opcodeTableModel.getRowCount(); i++) {
                String name = (String) opcodeTableModel.getValueAt(i, 0);
                String code = (String) opcodeTableModel.getValueAt(i, 1);
                String length = (String) opcodeTableModel.getValueAt(i, 2);
                opcodeData.append(name).append(" ").append(code).append(" ").append(length).append("\n");
            }
            
            try {
                if(asm == null){
                    JOptionPane.showMessageDialog(AssemblerGUI.this, "Первый проход не завершен.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Повторное создание/инициализация для второго прохода
                if(asm.isFirstPass()){ // Должен быть успешным для второго прохода
                
                if (asm.secondPass()) {
                    // Успех - заполняем объектный код
                    asm.getRelocationTable().entrySet().stream().sorted(
                        Map.Entry.comparingByKey(Comparator.comparing(Address::getAddress))
                    )
                    .forEach(entry -> {
                        relTabModel.addRow(new Object[]{
                            String.format("%06X", entry.getKey().getAddress()),
                            (entry.getValue()!=null)?entry.getValue():"",
                            asm.getCodeLines().getFirst().getLabel()
                        });
                    });

                    asm.getExternalLinks().entrySet().stream().sorted(
                        Map.Entry.comparingByValue(Comparator.comparing(Address::getAddress))
                    ).forEach(entry -> {
                        extTabModel.addRow(new Object[]{
                            entry.getKey(),
                            String.format("%06X", entry.getValue().getAddress()),
                            asm.getCodeLines().getFirst().getLabel()
                        });
                    });
                    
                    for(Segment s:asm.getSegments()){
                        s.getRelocationTable().entrySet().stream().sorted(
                            Map.Entry.comparingByKey(Comparator.comparing(Address::getAddress))
                        )
                        .forEach(entry -> {
                            relTabModel.addRow(new Object[]{
                                String.format("%06X", entry.getKey().getAddress()),
                                (entry.getValue()!=null)?entry.getValue():"",
                                s.getName()
                            });
                        });

                        s.getExternalLinks().entrySet().stream().sorted(
                            Map.Entry.comparingByValue(Comparator.comparing(Address::getAddress))
                        ).forEach(entry -> {
                            extTabModel.addRow(new Object[]{
                                entry.getKey(),
                                String.format("%06X", entry.getValue().getAddress()),
                                s.getName()
                            });
                        });
                    }

                    objectCodeArea.setText(asm.getObjText());
                    //JOptionPane.showMessageDialog(AssemblerGUI.this, "Второй проход завершен успешно.", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Ошибка - заполняем область ошибок
                    errorsPass2Area.setText(Errors.getPart2());
                    JOptionPane.showMessageDialog(AssemblerGUI.this, "Обнаружены ошибки во втором проходе.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(AssemblerGUI.this, "Первый проход завершен с ошибками.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                
            }
            } catch (HeadlessException ex) {
                errorsPass2Area.setText("Критическая ошибка: " + ex.getMessage());
            }
        }
    }
    
    
    private Assembler createAssemblerInstance(String sourceCode, String opcodeData) {
        
        Errors.setPart1("");
        Errors.setPart2("");

        return new Assembler(sourceCode,opcodeData,currentType);
    }

    public static void main(String[] args) {
        // Запуск GUI в потоке диспетчеризации событий
        SwingUtilities.invokeLater(() -> {
            new AssemblerGUI().setVisible(true);
        });
    }
}
