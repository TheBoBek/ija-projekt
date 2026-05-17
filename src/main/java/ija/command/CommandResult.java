/** Author: xmarina00 */
package ija.command;

/**
 * Result of one command execution.
 */
public record CommandResult<T>(
    T value,
    boolean stateChanged
) {
}
