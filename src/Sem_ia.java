import java.io.*;
import java.util.Scanner;

public class Sem_ia {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o nome do trabalhador:");
        String nome = scanner.nextLine();

        System.out.println("Digite o salário bruto:");
        double salarioBruto = Double.parseDouble(scanner.nextLine());

        System.out.println("Digite o desconto do INSS:");
        double descontoINSS = Double.parseDouble(scanner.nextLine());

        System.out.println("Digite o número de dependentes:");
        int numeroDependentes = Integer.parseInt(scanner.nextLine());

        System.out.println("Digite o valor total de descontos cabíveis para dedução de IRRF:");
        double totalDescontosIRRF = Double.parseDouble(scanner.nextLine());

        System.out.println("Digite o CPF:");
        String cpf = scanner.nextLine();

        if (!CPFValidator.validarCPF(cpf)) {
            System.out.println("CPF inválido.");
            return;
        }

        System.out.println("Digite o CEP:");
        String cep = scanner.nextLine();

        String enderecoCompleto = AddressService.obterEndereco(cep);

        double irrf = IRRFCalculator.calcularIRRF(salarioBruto, descontoINSS, numeroDependentes, totalDescontosIRRF);
        double salarioLiquido = salarioBruto - descontoINSS - irrf;

        Worker worker = new Worker(nome, salarioBruto, descontoINSS, numeroDependentes, totalDescontosIRRF, cpf, cep, enderecoCompleto, salarioLiquido);

        System.out.println(worker);

        try {
            FileManager.armazenarDados(worker);
        } catch (IOException e) {
            System.err.println("Erro ao armazenar dados: " + e.getMessage());
        }
    }

    static class Worker {
        private String nome;
        private double salarioBruto;
        private double descontoINSS;
        private int numeroDependentes;
        private double totalDescontosIRRF;
        private String cpf;
        private String cep;
        private String enderecoCompleto;
        private double salarioLiquido;

        public Worker(String nome, double salarioBruto, double descontoINSS, int numeroDependentes,
                      double totalDescontosIRRF, String cpf, String cep, String enderecoCompleto, double salarioLiquido) {
            this.nome = nome;
            this.salarioBruto = salarioBruto;
            this.descontoINSS = descontoINSS;
            this.numeroDependentes = numeroDependentes;
            this.totalDescontosIRRF = totalDescontosIRRF;
            this.cpf = cpf;
            this.cep = cep;
            this.enderecoCompleto = enderecoCompleto;
            this.salarioLiquido = salarioLiquido;
        }

        public String getCpf() {
            return cpf;
        }

        @Override
        public String toString() {
            return "Nome: " + nome + "\n" +
                   "Salário Bruto: " + salarioBruto + "\n" +
                   "Desconto INSS: " + descontoINSS + "\n" +
                   "IRRF: " + IRRFCalculator.calcularIRRF(salarioBruto, descontoINSS, numeroDependentes, totalDescontosIRRF) + "\n" +
                   "Salário Líquido: " + salarioLiquido + "\n" +
                   "Endereço: " + enderecoCompleto;
        }

        public String toFileString() {
            return nome + ";" + salarioBruto + ";" + descontoINSS + ";" + numeroDependentes + ";" + totalDescontosIRRF + ";" + cpf + ";" + cep + ";" + enderecoCompleto + ";" + salarioLiquido;
        }
    }

    static class FileManager {
        private static final String FILE_PATH = "dados_trabalhadores.txt";

        public static void armazenarDados(Worker worker) throws IOException {
            File arquivo = new File(FILE_PATH);
            StringBuilder conteudo = new StringBuilder();

            if (arquivo.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(arquivo));
                String linha;
                boolean trabalhadorExiste = false;

                while ((linha = br.readLine()) != null) {
                    if (linha.contains(worker.getCpf())) {
                        conteudo.append(worker.toFileString()).append("\n");
                        trabalhadorExiste = true;
                    } else {
                        conteudo.append(linha).append("\n");
                    }
                }
                br.close();

                if (!trabalhadorExiste) {
                    conteudo.append(worker.toFileString()).append("\n");
                }
            } else {
                conteudo.append(worker.toFileString()).append("\n");
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo));
            bw.write(conteudo.toString());
            bw.close();
        }
    }

    static class CPFValidator {
        public static boolean validarCPF(String cpf) {
            cpf = cpf.replace(".", "").replace("-", "");

            if (cpf.length() != 11)
                return false;

            boolean todosDigitosIguais = true;
            for (int i = 1; i < 11 && todosDigitosIguais; i++) {
                if (cpf.charAt(i) != cpf.charAt(0))
                    todosDigitosIguais = false;
            }

            if (todosDigitosIguais || cpf.equals("12345678909"))
                return false;

            int[] multiplicador1 = {10, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] multiplicador2 = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

            String tempCpf = cpf.substring(0, 9);
            int soma = 0;

            for (int i = 0; i < 9; i++)
                soma += Integer.parseInt(String.valueOf(tempCpf.charAt(i))) * multiplicador1[i];

            int resto = soma % 11;
            resto = resto < 2 ? 0 : 11 - resto;

            String digito = String.valueOf(resto);
            tempCpf += digito;
            soma = 0;

            for (int i = 0; i < 10; i++)
                soma += Integer.parseInt(String.valueOf(tempCpf.charAt(i))) * multiplicador2[i];

            resto = soma % 11;
            resto = resto < 2 ? 0 : 11 - resto;

            digito += String.valueOf(resto);

            return cpf.endsWith(digito);
        }
    }

    static class IRRFCalculator {
        public static double calcularIRRF(double salarioBruto, double descontoINSS, int numeroDependentes, double totalDescontosIRRF) {
            double baseCalculo = salarioBruto - descontoINSS - (numeroDependentes * 189.59) - totalDescontosIRRF;
            double irrf = 0;

            if (baseCalculo <= 1903.98)
                irrf = 0;
            else if (baseCalculo <= 2826.65)
                irrf = (baseCalculo - 1903.98) * 0.075 - 142.80;
            else if (baseCalculo <= 3751.05)
                irrf = (baseCalculo - 2826.65) * 0.15 - 354.80;
            else if (baseCalculo <= 4664.68)
                irrf = (baseCalculo - 3751.05) * 0.225 - 636.13;
            else
                irrf = (baseCalculo - 4664.68) * 0.275 - 869.36;

            return irrf;
        }
    }

    static class AddressService {
        public static String obterEndereco(String cep) {
            // Simulação da obtenção de endereço
            return "Endereço fictício para o CEP " + cep;
        }
    }
}
