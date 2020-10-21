 
sound: sound.cpp
	g++ sound.cpp -o sound -lSDL2 -lSDL2_mixer -lorts -g -rdynamic
	
sound.exe: sound.cpp
	i686-w64-mingw32-g++ sound.cpp -o sound.exe -lSDL2 -lSDL2_mixer -L orts -I . -lorts -lwsock32 -D RELEASE
