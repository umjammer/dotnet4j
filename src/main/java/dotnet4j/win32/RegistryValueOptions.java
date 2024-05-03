package dotnet4j.win32;

public enum RegistryValueOptions {
    /** optional behavior is unspecified */
    None,
    /**
     * the value of the type is retrieved without expanding embedded environment variables.
     *
     * @see "F:Microsoft.Win32.RegistryValueKind.ExpandString"
     */
    DoNotExpandEnvironmentNames
}
