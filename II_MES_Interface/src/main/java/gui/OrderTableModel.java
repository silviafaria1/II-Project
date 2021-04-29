package gui;

import javax.swing.table.DefaultTableModel;

public class OrderTableModel extends DefaultTableModel {
    static final String[] columnNames = {"ID", "Tipo", "Estado", "Hora de Entrada", "Hora de Início",
            "Hora de fim", "Folga", "Peças em execução", "Peças concluídas", "Peças pendentes"};

    OrderTableModel() {
        super(columnNames, 0);
    }



}
