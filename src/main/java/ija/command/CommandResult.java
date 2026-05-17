/**
 * Autoři a změny podle commit historie:
 * - alegzoo (2026-05-17)
 *   - 2026-05-17: Add command classes
 *
 * Popis obsahu:
 * - Zdrojový soubor CommandResult v balíku ija.command.
 */
package ija.command;

/**
 * Result of one command execution.
 */
public record CommandResult<T>(
    T value,
    boolean stateChanged
) {
}
