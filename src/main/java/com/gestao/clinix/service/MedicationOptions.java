package com.gestao.clinix.service;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MedicationOptions {
    public record Option(String value, String label) {}
    public record Options(List<Option> units, List<Option> categories,
            List<Option> presentationFormats) {}

    private static final Options OPTIONS = new Options(
            List.of(new Option("mg", "mg — Miligrama"),
                    new Option("mg/mL", "mg/mL — Miligrama por mililitro"),
                    new Option("g", "g — Grama"), new Option("%", "% — Porcentagem"),
                    new Option("mgc", "mgc — Micrograma"),
                    new Option("UI", "UI — Unidade internacional"),
                    new Option("UI/mL", "UI/mL — Unidade internacional por mililitro"),
                    new Option("mcg/mL", "mcg/mL — Micrograma por mililitro"),
                    new Option("mg/g", "mg/g — Miligrama por grama"),
                    new Option("mEq/mL", "mEq/mL — Miliequivalente por mililitro")),
            List.of(new Option("ANALGESIC", "Analgésico"),
                    new Option("ANTIPYRETIC", "Antitérmico"),
                    new Option("ANTI_INFLAMMATORY", "Anti-inflamatório"),
                    new Option("ANTIHISTAMINE", "Antialérgico"),
                    new Option("ANTIBIOTIC", "Antibiótico"), new Option("OTHER", "Outros")),
            List.of(new Option("TABLET", "Comprimido"), new Option("CAPSULE", "Cápsula"),
                    new Option("ORAL_SOLUTION", "Solução oral"),
                    new Option("ORAL_SUSPENSION", "Suspensão oral"),
                    new Option("DROPS", "Gotas"),
                    new Option("INJECTABLE_SOLUTION", "Solução injetável"),
                    new Option("CREAM", "Creme"), new Option("OINTMENT", "Pomada"),
                    new Option("OTHER", "Outros")));

    public Options getAll() { return OPTIONS; }

    public void validate(String value, List<Option> options, String field) {
        if (options.stream().noneMatch(option -> option.value().equals(value))) {
            throw new IllegalArgumentException("Selecione uma opção válida para " + field + ".");
        }
    }
}
