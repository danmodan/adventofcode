package com.danmodan.adventofcode.day6.broker;

import com.danmodan.adventofcode.day6.model.*;

public interface CommandListerner {
    
    void readCommand(Command command);
}
