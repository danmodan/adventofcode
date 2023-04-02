package com.danmodan.adventofcode.day6.broker;

import java.util.HashSet;
import java.util.Set;
import com.danmodan.adventofcode.day6.model.*;

public class CommandPublisher {

    private final Set<CommandListerner> listeners = new HashSet<>();
}
