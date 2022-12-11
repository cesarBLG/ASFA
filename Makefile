 
sound: sound.cpp
	g++ -D_GLIBCXX_DEBUG sound.cpp -o sound -lSDL2 -lSDL2_mixer -lorts -g -rdynamic
	
sound.exe: sound.cpp
	x86_64-w64-mingw32-g++ -D_GLIBCXX_DEBUG sound.cpp -o sound.exe -g -lSDL2 -lSDL2_mixer -L orts -I . -lorts -lwsock32
