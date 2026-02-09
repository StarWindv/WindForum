sudo rm ~/.local/bin/lines-counter ~/.local/bin/lines -f
sudo rm /usr/local/share/man/man1/lines.1 /usr/local/share/man/man1/lines-counter.1 -f

clang++ src/main.cpp -o lines -std=c++23 -Wall
strip lines
mv lines ~/.local/bin
ln ~/.local/bin/lines ~/.local/bin/lines-counter -s

sudo mkdir -p /usr/local/share/man/man1

sudo cp src/lines.1 /usr/local/share/man/man1/lines.1
sudo ln /usr/local/share/man/man1/lines.1 /usr/local/share/man/man1/lines-counter.1 -s
