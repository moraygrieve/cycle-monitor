REM Cycle Monitor, Copyright (C) 2015  M.B.Grieve
REM This file is part of the Cycle Monitor example application.

REM Cycle Monitor is free software: you can redistribute it and/or modify
REM it under the terms of the GNU General Public License as published by
REM the Free Software Foundation, either version 3 of the License, or
REM (at your option) any later version.

REM Cycle Monitor is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU General Public License for more details.

REM You should have received a copy of the GNU General Public License
REM along with City Monitor.  If not, see <http://www.gnu.org/licenses/>.

REM Contact: moray.grieve@me.com

java -classpath "c:\Program Files\SoftwareAG\Apama 5.3\lib\engine_client5.3.jar";"c:\Program Files\SoftwareAG\Apama 5.3\lib\util5.3.jar";\Java\jython2.7.0\jython.jar -Dpython.home=c:\Java\jython2.7.0 -Dpython.path=..\..\..\python org.python.util.jython printer.py 15903 DV_StationAlert id,type,message
