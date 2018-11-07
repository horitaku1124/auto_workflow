package main

import (
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"os/signal"
)

func subProcess(cmd *exec.Cmd, stdout io.ReadCloser) {
	buffer := make([]byte, 1000)
	for {
		readSize, err3 := stdout.Read(buffer)
		if err3 != nil {
			// panic(err3)
			break
		}
		if readSize <= 0 {
			break
		}
		fmt.Printf("byte: %d - %s\n", readSize, string(buffer[:readSize]))
	}
	fmt.Println("done")
	os.Exit(0)
}

func main() {
	args := make([]string, len(os.Args)-2)
	for i := 2; i < len(os.Args); i++ {
		args[i-2] = os.Args[i]
	}
	cmd := exec.Command(os.Args[1], args...)
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		log.Fatal(err)
	}
	err2 := cmd.Start()
	if err2 != nil {
		panic(err2)
	}
	go subProcess(cmd, stdout)
	fmt.Printf("PID=%d\n", os.Getpid())
	fmt.Printf("PID=%d\n", cmd.Process.Pid)
	// defer cmd.Wait()
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)

	// Block until a signal is received.
	s := <-c
	fmt.Println("Got signal:", s)
	err4 := cmd.Process.Signal(os.Interrupt)
	fmt.Println("Wait")
	if err4 != nil {
		panic(err4)
	}
	defer cmd.Wait()
}
